package com.matejdro.pebbletextsync.detekt

import dev.detekt.api.Config
import dev.detekt.test.lint
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.string.shouldContain
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

class UseActionLoggerInViewModelsTest {
   private val rule = UseActionLoggerInViewModels(Config.empty)

   @Test
   fun `Warn about ViewModel not using actionLogger`() {
      @Language("kotlin")
      val code = """
         class ViewModel(actionLogger: ActionLogger) {
            fun loadData() {
               
            }
         }
      """.trimIndent()

      val findings = rule.lint(code)

      findings.shouldHaveSize(1).first().apply {
         message shouldContain "does not start with a logAction() call"
         message shouldContain "loadData"
      }
   }

   @Test
   fun `Do not warn about ViewModel when using actionLogger`() {
      @Language("kotlin")
      val code = """
         class ViewModel(actionLogger: ActionLogger) {
            fun loadData() {
               actionLogger.logAction { "Hello" }
            }
         }
      """.trimIndent()

      val findings = rule.lint(code)

      findings.shouldBeEmpty()
   }

   @Test
   fun `Do not warn about ViewModel when using actionLogger inside launch scope`() {
      @Language("kotlin")
      val code = """
         class ViewModel(actionLogger: ActionLogger){
            fun loadData()  = GlobalScope.launch {
               actionLogger.logAction { "Hello" }
            }
         }
      """.trimIndent()

      val findings = rule.lint(code)

      findings.shouldBeEmpty()
   }

   @Test
   fun `Do not warn about ViewModel when the function is private`() {
      @Language("kotlin")
      val code = """
         class ViewModel(actionLogger: ActionLogger) {
            private fun loadData() {
               
            }
         }
      """.trimIndent()

      val findings = rule.lint(code)

      findings.shouldBeEmpty()
   }

   @Test
   fun `Do not warn when class is not a view model`() {
      @Language("kotlin")
      val code = """
         class ARepository(actionLogger: ActionLogger) {
            fun loadData() {
               
            }
         }
      """.trimIndent()

      val findings = rule.lint(code)

      findings.shouldBeEmpty()
   }

   @Test
   fun `Do not warn when class is a fake view model`() {
      @Language("kotlin")
      val code = """
         class MyViewModelFake(actionLogger: ActionLogger) {
            fun loadData() {
               
            }
         }
      """.trimIndent()

      val findings = rule.lint(code)

      findings.shouldBeEmpty()
   }

   @Test
   fun `Warn about ViewModelImpl not using actionLogger`() {
      @Language("kotlin")
      val code = """
         class ViewModelImpl(actionLogger: ActionLogger) {
            fun loadData() {
               
            }
         }
      """.trimIndent()

      val findings = rule.lint(code)

      findings.shouldHaveSize(1).first().apply {
         message shouldContain "does not start with a logAction() call"
         message shouldContain "loadData"
      }
   }
}
