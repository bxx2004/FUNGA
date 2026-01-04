package cn.revoist.lifephoton.module.aiassistant.impl.tools

import java.io.OutputStream
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author 6hisea
 * @date  2025/11/19 15:14
 * @description: None
 */
class StreamLocker(private val stream: OutputStream) {
    private val cache = CopyOnWriteArrayList<ByteArray>()
    private var state = false

    fun lock() {
        state = true
    }

    fun unlock() {
        // 先获取当前缓存的数据
        val dataToWrite = cache.toList()

        // 立即清空缓存，避免并发问题
        cache.clear()

        // 将之前缓存的数据写入流
        dataToWrite.forEach {
            stream.write(it)
            stream.flush()
        }

        state = false
    }

    fun write(data: ByteArray) {
        if (state) {
            cache.add(data)
        } else {
            stream.write(data)
            stream.flush()
        }
    }
}