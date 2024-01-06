package net.joeruPOGsan.translationmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.joeruPOGsan.translationmod.TranslationPipeLine.api.GoogleTransAPI;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TranslationModClient implements ClientModInitializer {
	private final GoogleTransAPI translator = new GoogleTransAPI();
	public void onInitializeClient() {
		// Register the onReceiveChatMessage event
		ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
            // Your custom code here
            String translatedMessage;
			String[] messageParserList;
			String username;
			String UserMsg;
            //				System.out.println(sanitizeString(message.getString()) + "STRING PARSED INTO API");
            messageParserList = parseMessageAsyncAtomic(message.getString());
            assert messageParserList != null;
            username = messageParserList[0];
            UserMsg = messageParserList[1];
            try {
                translatedMessage = translateWithGTranslateAsyncAtomic(UserMsg);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            sendTranslatedMessageToPlayer("<"+username+"> "+ translatedMessage);
        });
	}
	public static String[] sanitizeString(String input) {
		// Remove square brackets at the beginning and end of the input

		// Define the regular expression pattern
		String pattern = "<([^>]*)>(.*)";
		Pattern regex = Pattern.compile(pattern);
		Matcher matcher = regex.matcher(input);

		// Check if the pattern is matched
		if (matcher.matches()) {
			// Extract the substrings using groups
			String charsWithinAngleBrackets = matcher.group(1);
			String textAfterAngleBrackets = matcher.group(2);

			// Remove angle brackets from the beginning and end of the NAME field
			textAfterAngleBrackets = textAfterAngleBrackets.replaceAll("^<|>$", "");

			// Return the results
			return new String[]{charsWithinAngleBrackets, textAfterAngleBrackets};
		} else {
			// Return null for invalid input format
			return null;
		}
	}
//	private String translateWithGTranslate(String text) throws IOException {
//		List<String> result = translator.translate(text, "auto", "en");
//		System.out.println(result.get(0));
//		return result.get(0);
//	}

	private String translateWithGTranslateAsync(String text) {
		String[] translatedResult = { "" };

		// Create a new thread for the translation
		Thread translationThread = new Thread(() -> {
			try {
				List<String> result = translator.translate(text, "auto", "en");
				System.out.println(result.get(0));

				// Synchronize the translation result to ensure thread safety
				synchronized (translatedResult) {
					translatedResult[0] = result.get(0);
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});

		// Start the thread
		translationThread.start();

		// Wait for the translation thread to finish
		try {
			translationThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return translatedResult[0];
	}

	private String[] parseMessageAsync(String input) {
		String[] parsedResult = { "", "" };

		// Create a new thread for the regex part
		Thread regexThread = new Thread(() -> {
			// Synchronize the regex part to ensure thread safety
			synchronized (parsedResult) {
				parsedResult[0] = Objects.requireNonNull(sanitizeString(input))[0];
				parsedResult[1] = Objects.requireNonNull(sanitizeString(input))[1];
			}
		});

		// Start the thread
		regexThread.start();

		// Wait for the regex thread to finish
		try {
			regexThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return parsedResult;
	}

	private String translateWithGTranslateAsyncAtomic(String text) throws IOException {
		AtomicReference<String> translatedResult = new AtomicReference<>("");

		Thread translationThread = new Thread(() -> {
			try {
				List<String> result = translator.translate(text, "auto", "en");
				System.out.println(result.get(0));
				translatedResult.set(result.get(0));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});

		translationThread.start();

		try {
			translationThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return translatedResult.get();
	}

	private String[] parseMessageAsyncAtomic(String input) {
		AtomicReference<String[]> parsedResult = new AtomicReference<>(new String[]{"", ""});

		Thread regexThread = new Thread(() -> {
			parsedResult.set(sanitizeString(input));
		});

		regexThread.start();

		try {
			regexThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return parsedResult.get();
	}


	private void sendTranslatedMessageToPlayer(String translatedMessage) {
		// Send the translated message to the player's chat
		//Text chatMessage = Text.of(translatedMessage);
		// Replace with your method to send the message to the player's chat
		 //layer.sendMessage(chatMessage);
		Text chatMessage = Text.of(translatedMessage).copy().formatted(Formatting.YELLOW);
		System.out.println(chatMessage + "CHATMSG SENDING");
		MinecraftClient client = MinecraftClient.getInstance();

        assert client.player != null;
        client.player.sendMessage(chatMessage);
//		if (client != null && client.player != null){
//			Text chatMessage = Text.of(translatedMessage).copy().formatted(Formatting.YELLOW);
//
//			client.player.sendMessage(chatMessage);
//		}
	}}