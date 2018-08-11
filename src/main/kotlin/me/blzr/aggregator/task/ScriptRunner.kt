package me.blzr.aggregator.task

import org.springframework.core.io.ClassPathResource

object ScriptRunner {
    private const val SCRIPT = "/script.php"

    private fun loadScript(path: String) =
            ClassPathResource(path).inputStream.bufferedReader().readText()

    fun executeScript(stdin: String): String {
        val process = ProcessBuilder("php", "-r", loadScript(SCRIPT)).start()
        val writer = process.outputStream.writer()
        writer.write(stdin)
        writer.close()
        val stdout = process.inputStream.bufferedReader().readText()
        return stdout
    }

}
