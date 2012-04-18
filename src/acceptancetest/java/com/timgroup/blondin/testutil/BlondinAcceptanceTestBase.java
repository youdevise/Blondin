package com.timgroup.blondin.testutil;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import com.timgroup.blondin.Blondin;

import static java.lang.String.format;

public class BlondinAcceptanceTestBase {

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();
    
    private final String expensiveResourcesUrl = "my/expensive/resources/";
    private static final class BlondinTestContext {
        private static int blondinPort = 23453;
        private static int targetPort = 34297;
    }
    
    @Before
    public final void startBlondin() throws Exception {
        final String blondinPortString = String.valueOf(BlondinTestContext.blondinPort);
        final String targetPortString = String.valueOf(BlondinTestContext.targetPort);

        final File config = testFolder.newFile("blondinconf.properties");
        final Properties prop = new Properties();
        prop.setProperty("port", blondinPortString);
        prop.setProperty("targetHost", "localhost");
        prop.setProperty("targetPort", targetPortString);
        prop.setProperty("expensiveResourcesPath", expensiveResourcesUrl);
        
        prop.store(new FileOutputStream(config), null);
 
        beforeBlondinStarts();
        Blondin.main(new String[] {config.getAbsolutePath()});
        TrivialHttpClient.waitForSocket("localhost", BlondinTestContext.blondinPort);
    }

    protected void beforeBlondinStarts() throws Exception { }

    @After
    public final void stopBlondin() throws Exception {
        TrivialHttpClient.post(format("http://localhost:%s/stop", BlondinTestContext.blondinPort));
        BlondinTestContext.blondinPort++;
        BlondinTestContext.targetPort++;
    }
    
    public final int blondinPort() {
        return BlondinTestContext.blondinPort;
    }
    
    public final String blondinUrl() {
        return format("http://localhost:" + BlondinTestContext.blondinPort);
    }
    
    public final String expensiveResourcesPath() {
        return expensiveResourcesUrl;
    }
    
    public final int targetPort() {
        return BlondinTestContext.targetPort;
    }
}
