// https://stackoverflow.com/a/4115972
import org.junit.runner.JUnitCore

result = JUnitCore.runClasses \
    ExampleTests, \
    SetEnvTests, \
    AlignMethodsTests, \
    BamParserTests, \
    ResourceTests

String message = "Ran: " + result.getRunCount() + ", Ignored: " + result.getIgnoreCount() + ", Failed: " + result.getFailureCount()
if (result.wasSuccessful()) {
    println "SUCCESS! " + message
} else {
    println "FAILURE! " + message
    result.getFailures().each {
        println "Test Failure: ${it.getTestHeader()}"
        if (it.getException() instanceof java.lang.AssertionError) {
            // We don't care about the stack trace of assertion errors
            println it.getException()
        } else {
            // This will include the exception's message _and_ the traceback
            println it.getTrace()
        }
    }
    System.exit(1)
}
