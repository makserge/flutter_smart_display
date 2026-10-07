package com.smsoft.smartdisplay.utils.mpd

import android.os.SystemClock
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.Closeable
import java.io.EOFException
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException
import java.nio.charset.StandardCharsets

/** TCP connect timeout. Without one, an unreachable host blocked the MPD thread for minutes. */
const val MPD_CONNECT_TIMEOUT_MS = 5_000

/** Wait for "OK MPD x.y.z": a hung server or another service on the port must not block forever. */
const val MPD_GREETING_TIMEOUT_MS = 5_000

/** Wait for the answer to a command. */
const val MPD_COMMAND_TIMEOUT_MS = 10_000

/** Base class of the MPD client errors. */
open class MpdException(message: String, cause: Throwable? = null) : Exception(message, cause)

/** The server is unreachable, refused the connection or dropped it. */
class MpdConnectionException(message: String, cause: Throwable? = null) : MpdException(message, cause)

/**
 * No answer in time. The connection is closed: a late answer must not be read as the answer to
 * the next command.
 */
class MpdTimeoutException(message: String, cause: Throwable? = null) : MpdException(message, cause)

/** Wrong settings: no host, invalid port, not an MPD server, or the password was rejected. */
class MpdSetupException(message: String, cause: Throwable? = null) : MpdException(message, cause)

/** "ACK [error@command_listNum] {command} message". The connection stays usable. */
class MpdAckException(val code: Int, val command: String, message: String) : MpdException(message) {
    companion object {
        const val ACK_ERROR_PASSWORD = 3
        const val ACK_ERROR_PERMISSION = 4
        private val ACK = Regex("""^ACK \[(\d+)@\d+] \{([^}]*)} ?(.*)$""")

        fun parse(line: String): MpdAckException {
            val match = ACK.find(line) ?: return MpdAckException(0, "", line)
            return MpdAckException(match.groupValues[1].toInt(), match.groupValues[2], match.groupValues[3])
        }
    }
}

/**
 * One connection speaking the MPD text protocol, with a timeout on every blocking step.
 * Not thread-safe, except [close], which any thread may call to end a blocked read (that is how
 * the idle monitor is stopped).
 */
class Connection(
    private val host: String,
    private val port: Int,
    private val password: String,
    private val commandTimeoutMs: Int = MPD_COMMAND_TIMEOUT_MS
) : Closeable {
    @Volatile
    private var socket: Socket? = null
    private var reader: BufferedReader? = null
    private var writer: OutputStream? = null

    var version: String? = null
        private set

    /** elapsedRealtime() of the last answer. MPD drops clients that stay silent (connection_timeout). */
    var lastAnswerAt = 0L
        private set

    val isConnected: Boolean
        get() = socket?.isClosed == false

    @Throws(MpdException::class)
    fun connect() {
        if (isConnected) {
            return
        }
        if (host.isBlank()) {
            throw MpdSetupException("MPD host is not set")
        }
        val address = try {
            InetSocketAddress(host, port)
        } catch (e: IllegalArgumentException) {
            throw MpdSetupException("Invalid MPD port $port", e)
        }
        if (address.isUnresolved) {
            throw MpdConnectionException("Unknown MPD host $host")
        }
        val newSocket = Socket()
        socket = newSocket
        try {
            newSocket.tcpNoDelay = true
            newSocket.keepAlive = true
            newSocket.connect(address, MPD_CONNECT_TIMEOUT_MS)
            reader = BufferedReader(InputStreamReader(newSocket.getInputStream(), StandardCharsets.UTF_8))
            writer = BufferedOutputStream(newSocket.getOutputStream())
            newSocket.soTimeout = MPD_GREETING_TIMEOUT_MS
        } catch (e: IOException) {
            close()
            throw MpdConnectionException("Cannot connect to MPD at $host:$port", e)
        }
        val greeting = try {
            readLine()
        } catch (e: SocketTimeoutException) {
            close()
            throw MpdSetupException("No MPD greeting from $host:$port", e)
        } catch (e: IOException) {
            close()
            throw MpdConnectionException("MPD at $host:$port closed the connection", e)
        }
        if (!greeting.startsWith(GREETING_PREFIX)) {
            close()
            throw MpdSetupException("Not an MPD server at $host:$port: $greeting")
        }
        version = greeting.removePrefix(GREETING_PREFIX)
        lastAnswerAt = SystemClock.elapsedRealtime()
        try {
            newSocket.soTimeout = commandTimeoutMs
        } catch (e: IOException) {
            close()
            throw MpdConnectionException("MPD connection lost", e)
        }
        if (password.isNotEmpty()) {
            try {
                command("password", password)
            } catch (e: MpdAckException) {
                close()
                throw MpdSetupException("MPD rejected the password", e)
            }
        }
    }

    /** Sends one command and returns the answer lines without the final "OK". */
    @Throws(MpdException::class)
    fun command(name: String, vararg args: String): List<String> = exchange(name) {
        write(format(name, args))
        readAnswer()
    }

    /** Sends the commands as one "command_list_ok_begin" list and returns one answer per command. */
    @Throws(MpdException::class)
    fun commandList(vararg commands: List<String>): List<List<String>> = exchange("command list") {
        write(buildString {
            append("command_list_ok_begin\n")
            for (command in commands) {
                append(format(command.first(), command.drop(1).toTypedArray())).append('\n')
            }
            append("command_list_end")
        })
        val answers = ArrayList<List<String>>(commands.size)
        var current = ArrayList<String>()
        while (true) {
            val line = readLine()
            when {
                line == "list_OK" -> {
                    answers.add(current)
                    current = ArrayList()
                }
                line == "OK" -> break
                line.startsWith("ACK ") -> throw MpdAckException.parse(line)
                else -> current.add(line)
            }
        }
        answers
    }

    /**
     * Waits in "idle" until one of [subsystems] changes and returns the changed names.
     * After [quietMs] without news the idle is cancelled with "noidle": a live server answers at
     * once, a vanished one (powered off, network gone) does not, which ends in
     * MpdTimeoutException after the command timeout. An empty list means "nothing changed".
     */
    @Throws(MpdException::class)
    fun idle(quietMs: Int, vararg subsystems: String): List<String> = exchange("idle") {
        write(format("idle", subsystems))
        val current = socket ?: throw EOFException("Not connected")
        val first = try {
            current.soTimeout = quietMs
            readLine()
        } catch (_: SocketTimeoutException) {
            null
        } finally {
            current.soTimeout = commandTimeoutMs
        }
        val lines = if (first == null) {
            write("noidle")
            readAnswer()
        } else {
            readAnswer(first)
        }
        lines.filter { it.startsWith(CHANGED_PREFIX) }.map { it.removePrefix(CHANGED_PREFIX) }
    }

    /** Any thread. */
    override fun close() {
        val current = socket ?: return
        socket = null
        try {
            current.close()
        } catch (_: IOException) {
        }
    }

    private inline fun <T> exchange(what: String, block: () -> T): T {
        if (!isConnected) {
            throw MpdConnectionException("MPD is not connected")
        }
        try {
            return block().also { lastAnswerAt = SystemClock.elapsedRealtime() }
        } catch (e: MpdAckException) {
            lastAnswerAt = SystemClock.elapsedRealtime()
            throw e
        } catch (e: SocketTimeoutException) {
            close()
            throw MpdTimeoutException("MPD did not answer $what in time", e)
        } catch (e: IOException) {
            close()
            throw MpdConnectionException("MPD connection lost during $what", e)
        }
    }

    private fun readAnswer(firstLine: String? = null): List<String> {
        val lines = ArrayList<String>()
        var line = firstLine ?: readLine()
        while (true) {
            when {
                line == "OK" -> return lines
                line.startsWith("ACK ") -> throw MpdAckException.parse(line)
                else -> lines.add(line)
            }
            line = readLine()
        }
    }

    private fun readLine(): String =
        reader?.readLine() ?: throw EOFException("MPD closed the connection")

    private fun write(text: String) {
        val out = writer ?: throw EOFException("Not connected")
        out.write(text.toByteArray(StandardCharsets.UTF_8))
        out.write('\n'.code)
        out.flush()
    }

    private fun format(name: String, args: Array<out String>): String = buildString {
        append(name)
        for (arg in args) {
            append(" \"")
            append(arg.replace("\\", "\\\\").replace("\"", "\\\""))
            append('"')
        }
    }
}

private const val GREETING_PREFIX = "OK MPD "
private const val CHANGED_PREFIX = "changed: "
