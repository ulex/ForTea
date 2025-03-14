package com.jetbrains.fortea.lexer

import com.jetbrains.rider.test.base.psi.lexer.RiderFrontendLexerTest
import org.testng.annotations.Test

class T4RawLexerTest : RiderFrontendLexerTest("tt") {
  override fun createLexer() = T4Lexer()

  @Test
  fun `test assembly directive`() = doTest(
    """<#@ assembly name="Foo.dll" #>""",
    """
    |T4TokenType.DIRECTIVE_START ('<#@')
    |WHITE_SPACE (' ')
    |T4TokenType.TOKEN ('assembly')
    |WHITE_SPACE (' ')
    |T4TokenType.TOKEN ('name')
    |T4TokenType.EQUAL ('=')
    |T4TokenType.QUOTE ('"')
    |T4TokenType.RAW_ATTRIBUTE_VALUE ('Foo.dll')
    |T4TokenType.QUOTE ('"')
    |WHITE_SPACE (' ')
    |T4TokenType.BLOCK_END ('#>')
    |""".trimMargin()
  )

  @Test
  fun `test flat text`() = doTest(
    """Hello, world!
    |This is sample text.
    |""".trimMargin(),
    """
    |T4TokenType.RAW_TEXT ('Hello, world!')
    |T4TokenType.NEW_LINE ('\n')
    |T4TokenType.RAW_TEXT ('This is sample text.')
    |T4TokenType.NEW_LINE ('\n')
    |""".trimMargin()
  )

  @Test
  fun `test expression block`() = doTest(
    """<#= 2 + 2 #>""",
    """
    |T4TokenType.EXPRESSION_BLOCK_START ('<#=')
    |T4TokenType.RAW_CODE (' 2 + 2 ')
    |T4TokenType.BLOCK_END ('#>')
    |""".trimMargin()
  )

  @Test
  fun `test empty expression block`() = doTest(
    """<#=#>""",
    """
    |T4TokenType.EXPRESSION_BLOCK_START ('<#=')
    |T4TokenType.BLOCK_END ('#>')
    |""".trimMargin()
  )

  @Test
  fun `test expression block that starts with octothorpe`() = doTest(
    """<#=##>""",
    """
    |T4TokenType.EXPRESSION_BLOCK_START ('<#=')
    |T4TokenType.RAW_CODE ('#')
    |T4TokenType.BLOCK_END ('#>')""".trimMargin()
  )

  @Test
  fun `test special characters in flat text`() = doTest(
    """Hello, #123><2#@""",
    """T4TokenType.RAW_TEXT ('Hello, #123><2#@')""".trimMargin()
  )

  @Test
  fun `test complex file`() = doTest(
    """
    |<#@ output extension=".txt" #>
    |<# const int UPPER = 10; #>
    |<# for (int i = 0; i < UPPER; i += 1)
    |   { #>
    |<hello<#= i #>>
    |<#     PushIndent();
    |   } #>
    |<hello<#= UPPER #>/>
    |<# for (int i = 9; i >= 0; i += 1)
    |   {
    |     PopIndent(); #>
    |</hello<#= i #>>
    |<# } #>
    |""".trimMargin(),
    """
    |T4TokenType.DIRECTIVE_START ('<#@')
    |WHITE_SPACE (' ')
    |T4TokenType.TOKEN ('output')
    |WHITE_SPACE (' ')
    |T4TokenType.TOKEN ('extension')
    |T4TokenType.EQUAL ('=')
    |T4TokenType.QUOTE ('"')
    |T4TokenType.RAW_ATTRIBUTE_VALUE ('.txt')
    |T4TokenType.QUOTE ('"')
    |WHITE_SPACE (' ')
    |T4TokenType.BLOCK_END ('#>')
    |T4TokenType.NEW_LINE ('\n')
    |T4TokenType.STATEMENT_BLOCK_START ('<#')
    |T4TokenType.RAW_CODE (' const int UPPER = 10; ')
    |T4TokenType.BLOCK_END ('#>')
    |T4TokenType.NEW_LINE ('\n')
    |T4TokenType.STATEMENT_BLOCK_START ('<#')
    |T4TokenType.RAW_CODE (' for (int i = 0; i < UPPER; i += 1)\n   { ')
    |T4TokenType.BLOCK_END ('#>')
    |T4TokenType.NEW_LINE ('\n')
    |T4TokenType.RAW_TEXT ('<hello')
    |T4TokenType.EXPRESSION_BLOCK_START ('<#=')
    |T4TokenType.RAW_CODE (' i ')
    |T4TokenType.BLOCK_END ('#>')
    |T4TokenType.RAW_TEXT ('>')
    |T4TokenType.NEW_LINE ('\n')
    |T4TokenType.STATEMENT_BLOCK_START ('<#')
    |T4TokenType.RAW_CODE ('     PushIndent();\n   } ')
    |T4TokenType.BLOCK_END ('#>')
    |T4TokenType.NEW_LINE ('\n')
    |T4TokenType.RAW_TEXT ('<hello')
    |T4TokenType.EXPRESSION_BLOCK_START ('<#=')
    |T4TokenType.RAW_CODE (' UPPER ')
    |T4TokenType.BLOCK_END ('#>')
    |T4TokenType.RAW_TEXT ('/>')
    |T4TokenType.NEW_LINE ('\n')
    |T4TokenType.STATEMENT_BLOCK_START ('<#')
    |T4TokenType.RAW_CODE (' for (int i = 9; i >= 0; i += 1)\n   {\n     PopIndent(); ')
    |T4TokenType.BLOCK_END ('#>')
    |T4TokenType.NEW_LINE ('\n')
    |T4TokenType.RAW_TEXT ('</hello')
    |T4TokenType.EXPRESSION_BLOCK_START ('<#=')
    |T4TokenType.RAW_CODE (' i ')
    |T4TokenType.BLOCK_END ('#>')
    |T4TokenType.RAW_TEXT ('>')
    |T4TokenType.NEW_LINE ('\n')
    |T4TokenType.STATEMENT_BLOCK_START ('<#')
    |T4TokenType.RAW_CODE (' } ')
    |T4TokenType.BLOCK_END ('#>')
    |T4TokenType.NEW_LINE ('\n')
    |""".trimMargin()
  )

  @Test
  fun `test that lexer merges tokens`() = doTest(
    """
    |hello<<#@ output extension=".txt" #>
    |""".trimMargin(),
    """
    |T4TokenType.RAW_TEXT ('hello<')
    |T4TokenType.DIRECTIVE_START ('<#@')
    |WHITE_SPACE (' ')
    |T4TokenType.TOKEN ('output')
    |WHITE_SPACE (' ')
    |T4TokenType.TOKEN ('extension')
    |T4TokenType.EQUAL ('=')
    |T4TokenType.QUOTE ('"')
    |T4TokenType.RAW_ATTRIBUTE_VALUE ('.txt')
    |T4TokenType.QUOTE ('"')
    |WHITE_SPACE (' ')
    |T4TokenType.BLOCK_END ('#>')
    |T4TokenType.NEW_LINE ('\n')""".trimMargin()
  )
}
