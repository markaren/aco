package info.laht.aco.core

import info.laht.aco.math.inverse
import info.laht.aco.utils.Clock
import java.io.BufferedReader
import java.io.Closeable
import java.io.IOException
import java.io.InputStreamReader
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.FutureTask
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.properties.Delegates
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

private typealias Predicate = ((Engine) -> Boolean)
private typealias Callback = () -> Unit

interface EngineRunner {

    fun start()

    fun stop()

}

class DefaultEngineRunner(
    val engine: Engine
): EngineRunner {

    var paused = AtomicBoolean(false)
    private var timePaused = 0.0

    private var thread: Thread? = null
    private val stop = AtomicBoolean(false)

    var simulationClock: Double by Delegates.notNull()
        private set
    var wallClock: Double by Delegates.notNull()
        private set

    val enableRealTimeTarget = AtomicBoolean(true)

    var targetRealTimeFactor: Double by Delegates.observable(1.0) { _, _, newValue ->
        inverseTargetRealTimeFactor = newValue.inverse()
    }
    private var inverseTargetRealTimeFactor = targetRealTimeFactor.inverse()

    var actualRealTimeFactor: Double by Delegates.notNull()
        private set

    var callback: Callback? = null
    private var predicate: Predicate? = null

    val isStarted: Boolean
        get() {
            return thread != null
        }

    init {
        if (!engine.isInitialized) {
            engine.init()
        }
    }

    override fun start() {
        if (this.thread == null) {
            this.stop.set(false)
            this.thread = Thread(Runner()).apply { start() }
        } else {
            throw IllegalStateException("Start can only be invoked once!")
        }
    }

    fun runWhile(predicate: Predicate): Future<Unit> {
        check(!isStarted && this.predicate == null)
        this.predicate = predicate
        val executor = Executors.newCachedThreadPool()
        return FutureTask {
            start()
            this.thread!!.join()
            executor.shutdown()
        }.also {
            executor.submit(it)
        }
    }

    fun runUntil(timePoint: Number): Future<Unit> {
        val doubleTimePoint = timePoint.toDouble()
        return runWhile(
            predicate = { it.currentTime >= doubleTimePoint }
        )
    }

    fun runFor(time: Number): Future<Unit> {
        val doubleTime = time.toDouble()
        return runWhile(
            predicate = { it.currentTime + it.startTime >= doubleTime }
        )
    }

    override fun stop() {
        thread?.also {
            stop.set(true)
            it.join()
        }
    }

    private inner class Runner : Runnable {

        private val clock = Clock()

        @ExperimentalTime
        override fun run() {

            val inputThread = ConsoleInputReadTask().apply { start() }

            val t0 = System.currentTimeMillis()
            while (!stop.get() && predicate?.invoke(engine) != true) {

                if (!paused.get()) {

                    engine.step(clock.getDelta())

                    simulationClock = engine.currentTime - engine.startTime
                    wallClock = ((System.currentTimeMillis() - t0).toDouble() / 1000.0) - timePaused
                    actualRealTimeFactor = simulationClock / wallClock

                    if (enableRealTimeTarget.get()) {
                        val diff = (simulationClock * inverseTargetRealTimeFactor) - wallClock
                        if (diff > 0) {
                            val timeToSleep = (diff * 1000.0).toLong()
                            Thread.sleep(timeToSleep)
                        }
                    }

                    callback?.invoke()

                } else {
                    timePaused += measureTime { Thread.sleep(1L) }.inSeconds
                }
            }
            stop.set(true)

            inputThread.interrupt()
            inputThread.join()

        }

    }

    //https://stackoverflow.com/questions/4983065/how-to-interrupt-java-util-scanner-nextline-call
    inner class ConsoleInputReadTask : Thread() {

        @Throws(IOException::class)
        override fun run() {
            val br = BufferedReader(InputStreamReader(System.`in`))
            var quit = false
            println()
            println("Commandline options:\n")
//            println("\t 'r' -> Enable/disable realtime execution  ")
            println("\t 'p' -> Pause/unpause execution  ")
            println("\t 'q' -> Abort execution")
            println()
            do {
                val input: String? = try { // wait until we have data to complete a readLine()
                    while (!br.ready()) {
                        sleep(200)
                    }
                    br.readLine()
                } catch (e: InterruptedException) {
                    null
                }
                when (input) {
//                    "r" -> {
//                        enableRealTimeTarget.set(!enableRealTimeTarget.get())
//                        if (enableRealTimeTarget.get()) {
//                            println("Realtime target enabled, rtf=$targetRealTimeFactor")
//                        } else {
//                            println("Realtime target disabled")
//                        }
//                    }
                    "p" -> {
                        paused.set(!paused.get())
                        if (paused.get()) {
                            println("Execution paused at t=${engine.currentTime}")
                        } else {
                            println("Execution resumed")
                        }
                    }
                    null, "q" -> quit = true
                }
            } while (!quit)

            if (!stop.getAndSet(true)) {
                println("Manually aborted execution at t=${engine.currentTime}..")
            }

        }

    }

}

