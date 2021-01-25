package test;

import main.Mail;
import main.Server;

import org.junit.jupiter.api.Test;

public class ServerTests {

    @Test
    void testValidRequest(){
        Server.validRequest("", "", new Mail());
    }


}
