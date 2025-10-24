package com.sh.my.diskdir

import com.sh.my.diskdir.utils.FileSystemUtils
import org.junit.Test
import org.junit.Assert.*

class FileSystemUtilsTest {
    
    @Test
    fun testFormatFileSize() {
        assertEquals("0 B", FileSystemUtils.formatFileSize(0))
        assertEquals("1.0 B", FileSystemUtils.formatFileSize(1))
        assertEquals("1.0 KB", FileSystemUtils.formatFileSize(1024))
        assertEquals("1.0 MB", FileSystemUtils.formatFileSize(1024 * 1024))
        assertEquals("1.0 GB", FileSystemUtils.formatFileSize(1024 * 1024 * 1024))
    }
    
    @Test
    fun testIsDeviceAccessible() {
        // Тестируем с несуществующим путем
        assertFalse(FileSystemUtils.isDeviceAccessible("/nonexistent/path"))
        
        // Тестируем с корневым путем (должен существовать на большинстве систем)
        assertTrue(FileSystemUtils.isDeviceAccessible("/"))
    }
}
