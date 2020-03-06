package de.example.pearrunner;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import de.example.pearrunner.initializers.PearHandler;
import de.example.pearrunner.util.CasPool;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class PearRunnerApplication implements ApplicationRunner {

    public static void main(String[] args) {

	SpringApplication.run(PearRunnerApplication.class, args);
    }

    @Autowired
    private PearHandler pearInitializer;

    @Autowired
    private CasPool casPool;

    @Override
    public void run(ApplicationArguments args) throws Exception {

	String pearPath = this.getOneOption(args, "pearPath");
	String pearInstallDir = this.getOneOption(args, "pearInstallDir");
	this.pearInitializer.setPearPath(pearPath, pearInstallDir);
	this.casPool.initPool();

    }

    private String getOneOption(ApplicationArguments args, String optionName) {

	List<String> optionValues = args.getOptionValues(optionName);
	if (optionValues != null && optionValues.size() == 1) {
	    return optionValues.get(0);
	}
	PearRunnerApplication.log.error("Requires one path to a pear file via command line arg --pearPath'");
	return null;
    }

}
