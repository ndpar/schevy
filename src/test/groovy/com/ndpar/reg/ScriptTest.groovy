package com.ndpar.reg

import org.junit.Test

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

class ScriptTest {

    @Test
    void copySamplesToClipboard() {
        def script = getClass().getResource('/samples.scm').text.replace('\n', '').replaceAll(/\s+/, ' ')
        Toolkit.defaultToolkit.systemClipboard.setContents(new StringSelection(script), null)
    }
}
