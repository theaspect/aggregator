package me.blzr.aggregator

import java.io.File
import java.lang.ClassLoader.getSystemResource

object ScriptRunner {
    private const val SCRIPT = "script.php"

    private fun loadScript(path: String) =
            File(getSystemResource(path).file).readText()

    fun executeScript(stdin: String): String {
        val process = ProcessBuilder("php", "-r", loadScript(SCRIPT)).start()
        val writer = process.outputStream.writer()
        writer.write(stdin)
        writer.close()
        val stdout = process.inputStream.bufferedReader().readText()
        return stdout
    }

}
