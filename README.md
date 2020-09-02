# Bootiful Time SpringOne 2020

In this talk we'll look at how to build and test asynchronous services. The goal here is to get the benefits of asynchronous, concurrent programming, while dealing with `Thread`s, `Executor`s, and `Runnables` as little as possible. How can Spring help us to write more scalable, multithreaded, better behaved, and easily tested code? 

* [What do we mean by event-driven](https://martinfowler.com/articles/201701-event-driven.html)?
* `@Async` 
* Spring Integration and `@Gateway`
* RSocket and [Spring Retrosocket](https://github.com/spring-projects-experimental/spring-retrosocket)
* Spring Cloud Contract and Messaging Payloads 



## Notes

* martin fowler blog on events https://martinfowler.com/articles/201701-event-driven.html 
* Async 
 ** ApplicationListener<ApplicationReady> 
 ** AudioService#convertToMp3(File wav)
 ** Manual impl
 ** @Async impl


@Log4j2
abstract class AudioUtils {

    private static File deriveMp3FileForWavFile(File wav) {
        return new File(wav.getParentFile(), wav.getName().substring(0, wav.getName().length() - 4) + ".mp3");
    }

    @SneakyThrows
    public static File convert(File wav ) {
        var mp3 = deriveMp3FileForWavFile(wav);
        Assert.state(!mp3.exists() || mp3.delete(), () -> "the destination .mp3 file " + mp3.getAbsolutePath() + " must not exist!");
        log.info("converting " + wav.getAbsolutePath() + " to " + mp3.getAbsolutePath() + '.');
        var command = "ffmpeg -i " + wav.getAbsolutePath() + " " + mp3.getAbsolutePath();
        var exec = Runtime.getRuntime().exec(command);
        var statusCode = exec.waitFor();
        if (statusCode == 0) {
            log.info("converted " + wav.getAbsolutePath() + " to " + mp3.getAbsolutePath() + '.');
            return mp3;
        }
        throw new RuntimeException("could not convert '" + wav.getAbsolutePath() + "'!");
    }

}
