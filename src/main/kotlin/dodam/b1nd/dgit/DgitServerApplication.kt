package dodam.b1nd.dgit

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class DgitServerApplication

fun main(args: Array<String>) {
    runApplication<DgitServerApplication>(*args)
}
