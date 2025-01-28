package org.mantis.muse.services

import android.os.Build
import android.provider.ContactsContract.Directory
import androidx.annotation.RequiresApi
import java.io.File
import java.lang.NullPointerException
import java.nio.file.FileSystemException
import java.util.Deque

data class CrawlerOutput (
    val extension: String,
    val fileLocations: MutableList<String>
)

@RequiresApi(Build.VERSION_CODES.O)
fun fileCrawler(
    startDirectory: File,
    targetExtensions: List<String>
): Result<List<CrawlerOutput>> {
    if (!startDirectory.isDirectory){
        return Result.failure(FileSystemException("startDirectory is not a directory"))
    }
    val found = mutableMapOf<String, CrawlerOutput>().apply {
        targetExtensions.forEach{this[it] = CrawlerOutput(it, mutableListOf())}
    }
    val searchQueue = ArrayDeque<File>().apply {
        this.add(startDirectory)
    }

    while (!searchQueue.isEmpty()) {
        val target = searchQueue.removeFirst()
        if (!target.isDirectory){continue}

        try{
            for (file in target.listFiles()!!) {
                if (file.isFile) {
                    for (fileExtension in targetExtensions) {
                        if (file.name.endsWith(fileExtension)) {
                            found[fileExtension]!!.fileLocations.add(file.absolutePath)
                            println("FOUND")
                        }
                    }
                } else if (file.isDirectory) {
                    searchQueue.add(file)
                }
            }
        }catch(e:NullPointerException) {
//            println(e.stackTrace.)
            continue
        }
    }

    val out = mutableListOf<CrawlerOutput>()
    found.values.forEach {
        out.add(it)
    }
    return Result.success(out)
}