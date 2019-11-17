package ch.cordalo.template.client.webserver;

import ch.cordalo.template.CordaloTemplateBaseTests;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Starter.class,
        properties = {
                "config.rpc.host=", // remove hostname to NOT initialize CordaRPClient
                "config.rpc.port=10042",
                "config.rpc.username=user1",
                "config.rpc.password=test"
        })
@WebAppConfiguration
public abstract class CordaloControllerBaseTests extends CordaloTemplateBaseTests {
    protected MockMvc mvc;

    @Autowired
    WebApplicationContext webApplicationContext;
    protected void setUpSpringTests() {
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    public static String decode(String url)
    {
        try {
            String prevURL="";
            String decodeURL=url;
            while(!prevURL.equals(decodeURL))
            {
                prevURL=decodeURL;
                decodeURL= URLDecoder.decode( decodeURL, "UTF-8" );
            }
            return decodeURL;
        } catch (UnsupportedEncodingException e) {
            return "Issue while decoding" +e.getMessage();
        }
    }
    public static String encode(String url)
    {
        try {
            String encodeURL= URLEncoder.encode( url, "UTF-8" );
            return encodeURL;
        } catch (UnsupportedEncodingException e) {
            return "Issue while encoding" +e.getMessage();
        }
    }

}
