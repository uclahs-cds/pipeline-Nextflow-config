// https://stackoverflow.com/a/4115972
import org.junit.runner.JUnitCore

result = JUnitCore.runClasses SetEnvTests

String message = "Ran: " + result.getRunCount() + ", Ignored: " + result.getIgnoreCount() + ", Failed: " + result.getFailureCount()
if (result.wasSuccessful()) {
    println "SUCCESS! " + message
} else {
    println "FAILURE! " + message
    result.getFailures().each {
        println "Test Failure: ${it.getTestHeader()}"
        println it.getException()
    }
    System.exit(1)
}
