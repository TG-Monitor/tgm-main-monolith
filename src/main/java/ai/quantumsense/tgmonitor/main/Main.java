package ai.quantumsense.tgmonitor.main;

import ai.quantumsense.tgmonitor.backend.Interactor;
import ai.quantumsense.tgmonitor.backend.InteractorImpl;
import ai.quantumsense.tgmonitor.cli.Cli;
import ai.quantumsense.tgmonitor.entities.Emails;
import ai.quantumsense.tgmonitor.entities.EmailsImpl;
import ai.quantumsense.tgmonitor.entities.Patterns;
import ai.quantumsense.tgmonitor.entities.PatternsImpl;
import ai.quantumsense.tgmonitor.entities.Peers;
import ai.quantumsense.tgmonitor.entities.PeersImpl;
import ai.quantumsense.tgmonitor.matching.PatternMatcherImpl;
import ai.quantumsense.tgmonitor.monitor.LoginCodePrompt;
import ai.quantumsense.tgmonitor.monitor.Monitor;
import ai.quantumsense.tgmonitor.monitor.MonitorImpl;
import ai.quantumsense.tgmonitor.notification.NotificatorImpl;
import ai.quantumsense.tgmonitor.notification.format.FormatterImpl;
import ai.quantumsense.tgmonitor.notification.send.MailgunSender;
import ai.quantumsense.tgmonitor.servicelocator.ServiceLocator;
import ai.quantumsense.tgmonitor.servicelocator.instances.EmailsLocator;
import ai.quantumsense.tgmonitor.servicelocator.instances.InteractorLocator;
import ai.quantumsense.tgmonitor.servicelocator.instances.LoginCodePromptLocator;
import ai.quantumsense.tgmonitor.servicelocator.instances.MonitorLocator;
import ai.quantumsense.tgmonitor.servicelocator.instances.PatternsLocator;
import ai.quantumsense.tgmonitor.servicelocator.instances.PeersLocator;
import ai.quantumsense.tgmonitor.telegram.TelegramImpl;

public class Main {

    private static final String TG_API_ID = System.getenv("TG_API_ID");
    private static final String TG_API_HASH = System.getenv("TG_API_HASH");
    private static final String MAILGUN_API_KEY = System.getenv("MAILGUN_API_KEY");
    private static final String PHONE_NUMBER = System.getenv("PHONE_NUMBER");

    private static final String MAILGUN_DOMAIN = "quantumsense.ai";
    private static final String EMAIL_SENDING_ADDRESS = "tg-monitor@quantumsense.ai";
    private static final String EMAIL_SENDING_NAME = "TG-Monitor";


    public static void main(String[] args) {
        checkEnv();

        ServiceLocator<Peers> peersLocator = new PeersLocator();
        ServiceLocator<Patterns> patternsLocator = new PatternsLocator();
        ServiceLocator<Emails> emailsLocator = new EmailsLocator();

        new PeersImpl(peersLocator);
        new PatternsImpl(patternsLocator);
        new EmailsImpl(emailsLocator);

        ServiceLocator<Monitor> monitorLocator = new MonitorLocator();
        ServiceLocator<Interactor> interactorLocator = new InteractorLocator();
        ServiceLocator<LoginCodePrompt> loginCodePromptLocator = new LoginCodePromptLocator();

        new MonitorImpl(
                new TelegramImpl(TG_API_ID, TG_API_HASH, peersLocator, interactorLocator, loginCodePromptLocator),
                monitorLocator);

        new InteractorImpl(
                new PatternMatcherImpl(interactorLocator, patternsLocator),
                new NotificatorImpl(new FormatterImpl(), new MailgunSender(MAILGUN_API_KEY, MAILGUN_DOMAIN, EMAIL_SENDING_ADDRESS, EMAIL_SENDING_NAME), emailsLocator),
                interactorLocator);

        Cli cli = new Cli(peersLocator, patternsLocator, emailsLocator, monitorLocator, loginCodePromptLocator);
        cli.launch();
    }

    private static void checkEnv() {
        String missing = null;
        if (TG_API_ID == null) missing = "TG_API_ID";
        else if (TG_API_HASH == null) missing = "TG_API_HASH";
        else if (MAILGUN_API_KEY == null) missing = "MAILGUN_API_KEY";
        else if (PHONE_NUMBER == null) missing = "PHONE_NUMBER";
        if (missing != null)
            throw new RuntimeException("Must set " + missing + " environment variable");
    }
}
