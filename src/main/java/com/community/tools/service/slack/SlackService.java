package com.community.tools.service.slack;

import com.community.tools.service.MessageService;
import com.community.tools.service.discord.NameBlock;
import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.methods.request.users.UsersListRequest;
import com.github.seratch.jslack.api.methods.response.chat.ChatPostMessageResponse;
import com.github.seratch.jslack.api.model.Channel;
import com.github.seratch.jslack.api.model.Conversation;
import com.github.seratch.jslack.api.model.User;
import com.github.seratch.jslack.api.webhook.Payload;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Primary
public class SlackService implements MessageService {

  @Value("${slack.token}")
  private String token;
  @Value("${slack.webhook}")
  private String slackWebHook;

  private String text;

  /**
   * Send private message with messageText to username.
   *
   * @param username    Slack login
   * @param messageText Text of message
   * @return timestamp of message
   * @throws IOException       IOException
   * @throws SlackApiException SlackApiException
   */
  @Override
  public String sendPrivateMessage(String username, String messageText) {
    Slack slack = Slack.getInstance();
    try {
      ChatPostMessageResponse postResponse =
              slack.methods(token).chatPostMessage(
                  req -> req.channel(getIdByUsername(username)).asUser(true)
                              .text(messageText));
      return postResponse.getTs();
    } catch (IOException | SlackApiException exception) {
      throw new RuntimeException(exception);
    }
  }

  /**
   * Send block message with messageText to username.
   *
   * @param username    Slack login
   * @param messageText Text of message
   * @return timestamp of message
   * @throws IOException       IOException
   * @throws SlackApiException SlackApiException
   */
  @Override
  public String sendBlocksMessage(String username, String messageText) {
    Slack slack = Slack.getInstance();
    try {
      ChatPostMessageResponse postResponse = slack.methods(token).chatPostMessage(
          req -> req.channel(getIdByUsername(username)).asUser(true)
                      .blocksAsString(messageText));
      return postResponse.getTs();
    } catch (IOException | SlackApiException exception) {
      throw new RuntimeException(exception);
    }
  }

  /**
   * Send block message with messageText to username.
   *
   * @param username    Slack login
   * @param messageText List of message with code block
   * @return timestamp of message
   * @throws IOException       IOException
   * @throws SlackApiException SlackApiException
   */
  @Override
  public String sendBlocksMessage(String username, List<Map<NameBlock, String>> messageText) {
    Slack slack = Slack.getInstance();
    try {
      ChatPostMessageResponse postResponse = slack.methods(token).chatPostMessage(
          req -> req.channel(getIdByUsername(username)).asUser(true)
              .blocksAsString(createBlocksMessage(messageText)));
      return postResponse.getTs();
    } catch (IOException | SlackApiException exception) {
      throw new RuntimeException(exception);
    }
  }

  /**
   * Create block of message with code block.
   *
   * @param messageText List of message with code block
   * @return block As String
   */
  public String createBlocksMessage(List<Map<NameBlock, String>> messageText) {
    if ((messageText.get(0).get(NameBlock.BLOCKS)) != null) {
      return messageText.get(0).get(NameBlock.BLOCKS);
    }
    return null;
  }

  /**
   * Send attachment message with messageText to username.
   *
   * @param username    Slack login
   * @param messageText Text of message
   * @return timestamp of message
   * @throws IOException       IOException
   * @throws SlackApiException SlackApiException
   */
  @Override
  public String sendAttachmentsMessage(String username, String messageText) {
    Slack slack = Slack.getInstance();
    try {
      ChatPostMessageResponse postResponse =
          slack.methods(token).chatPostMessage(
              req -> req.channel(getIdByUsername(username)).asUser(true)
                  .attachmentsAsString(messageText));

      return postResponse.getTs();
    } catch (IOException | SlackApiException exception) {
      throw new RuntimeException(exception);
    }
  }

  /**
   * Send attachment message with messageText to channel.
   *
   * @param channelName Name of channel
   * @param messageText Text of message
   * @return timestamp of message
   * @throws IOException       IOException
   * @throws SlackApiException SlackApiException
   */
  @Override
  public String sendMessageToConversation(String channelName, String messageText) {
    Slack slack = Slack.getInstance();
    try {
      ChatPostMessageResponse postResponse =
          slack.methods(token).chatPostMessage(
              req -> req.channel(getIdByChannelName(channelName)).asUser(true).text(messageText));
      return postResponse.getTs();
    } catch (IOException | SlackApiException exception) {
      throw new RuntimeException(exception);
    }
  }

  /**
   * Send attachment message with blocks of Text to the channel.
   *
   * @param channelName Name of channel
   * @param messageText Blocks of message
   * @return timestamp of message
   * @throws IOException       IOException
   * @throws SlackApiException SlackApiException
   */
  @Override
  public String sendBlockMessageToConversation(String channelName, String messageText) {
    Slack slack = Slack.getInstance();
    try {
      ChatPostMessageResponse postResponse =
          slack.methods(token).chatPostMessage(
              req -> req.channel(getIdByChannelName(channelName))
                  .asUser(true).blocksAsString(messageText));
      return postResponse.getTs();
    } catch (IOException | SlackApiException exception) {
      throw new RuntimeException(exception);
    }
  }

  /**
   * Send attachment message with messageText to channel.
   *
   * @param channelName Name of channel
   * @param messageText Text of message
   * @return timestamp of message
   * @throws IOException       IOException
   * @throws SlackApiException SlackApiException
   */
  @Deprecated
  @Override
  public String sendMessageToChat(String channelName, String messageText) {
    Slack slack = Slack.getInstance();
    try {
      Channel channel = slack.methods(token)
          .channelsList(req -> req)
          .getChannels()
          .stream()
          .filter(u -> u.getName().equals(channelName))
          .findFirst().get();

      ChatPostMessageResponse postResponse =
          slack.methods(token).chatPostMessage(
              req -> req.channel(channel.getId()).asUser(true).text(messageText));
      return postResponse.getTs();
    } catch (IOException | SlackApiException exception) {
      throw new RuntimeException(exception);
    }
  }

  /**
   * Get Conversation by Slack`s channelName.
   *
   * @param channelName Slack`s channelName
   * @return id of Conversation
   */
  @Override
  public String getIdByChannelName(String channelName) {
    Slack slack = Slack.getInstance();
    try {
      Conversation channel = slack.methods(token)
          .conversationsList(req -> req)
          .getChannels()
          .stream()
          .filter(u -> u.getName().equals(channelName))
          .findFirst().get();

      return channel.getId();
    } catch (IOException | SlackApiException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Get user by Slack`s id.
   *
   * @param id Slack`s id
   * @return realName of User
   */
  @Override
  public String getUserById(String id) {
    Slack slack = Slack.getInstance();
    try {
      User user = slack.methods(token).usersList(req -> req).getMembers().stream()
              .filter(u -> u.getId().equals(id))
              .findFirst().get();
      return user.getProfile().getDisplayName();
    } catch (IOException | SlackApiException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Get user by Slack`s id.
   *
   * @param id Slack`s id
   * @return Slack`s id
   */
  @Override
  public String getIdByUser(String id) {
    Slack slack = Slack.getInstance();
    try {
      User user = slack.methods(token).usersList(req -> req).getMembers().stream()
              .filter(u -> u.getRealName().equals(id))
              .findFirst().get();
      return user.getId();
    } catch (IOException | SlackApiException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Get user by Slack`s username.
   *
   * @param username Slack`s id
   * @return Slack`s id
   */
  @Override
  public String getIdByUsername(String username) {
    Slack slack = Slack.getInstance();
    try {
      User user = slack.methods(token).usersList(req -> req).getMembers().stream()
          .filter(u -> u.getProfile().getDisplayName().equals(username))
          .findFirst().get();
      return user.getId();
    } catch (IOException | SlackApiException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Get all Slack`s user.
   *
   * @return Set of users.
   */
  @Override
  public Set<User> getAllUsers() {
    try {
      Slack slack = Slack.getInstance();
      Set<User> users = new HashSet<>(slack.methods()
              .usersList(UsersListRequest.builder()
                      .token(token)
                      .build())
              .getMembers());

      return users;
    } catch (IOException | SlackApiException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Send announcement with message.
   *
   * @param message Text of message
   */
  @Override
  public void sendAnnouncement(String message) {
    try {
      Payload payload = Payload.builder().text(message).build();
      Slack slack = Slack.getInstance();
      slack.send(slackWebHook, payload);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}