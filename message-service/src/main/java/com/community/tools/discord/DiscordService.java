package com.community.tools.discord;

import com.community.tools.model.ServiceUser;
import com.community.tools.service.MessageService;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.Button;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Data
@RequiredArgsConstructor
@Profile("discord")
public class DiscordService implements MessageService<MessageEmbed> {

  private final Button buttonWithEmbed = Button.primary("buttonEmbed", "Button");

  @Autowired
  private JDA jda;
  @Autowired
  private DiscordMessagingService discordMessagingService;

  /**
   * Send private message with messageText to username.
   *
   * @param username Discord login
   * @param messageText Text of message
   */
  @Override
  public void sendPrivateMessage(String username, String messageText) {
    jda.getUserById(getIdByUsername(username))
        .openPrivateChannel()
        .queue(
            (channel) -> {
              channel.sendMessage(messageText).queue();
            });
  }

  /**
   * Sends a message with embedded content to a user's private channel.
   *
   * @param username The username of the recipient.
   * @param message The MessageEmbed content to be sent.
   */
  @Override
  public void sendBlocksMessage(String username, MessageEmbed message) {
    jda.getUserById(getIdByUsername(username))
        .openPrivateChannel()
        .queue(
            (channel) -> {
              channel.sendMessageEmbeds(message).queue();
            });
  }

  /**
   * Sends a message with embedded content using an EmbedBuilder to a user's private channel.
   *
   * @param username The username of the recipient.
   * @param embedBuilder The EmbedBuilder containing the message's embedded content.
   */
  @Override
  public void sendBlocksMessage(String username, EmbedBuilder embedBuilder) {
    jda.getUserById(getIdByUsername(username))
        .openPrivateChannel()
        .queue(
            (channel) -> {
              discordMessagingService.splitAndSendEmbed(
                  channel.getId(), embedBuilder, buttonWithEmbed);
            });
  }

  /**
   * Sends a message with embedded content and attachments to a user's private channel.
   *
   * @param username The username of the recipient.
   * @param message The MessageEmbed content to be sent.
   */
  @Override
  public void sendAttachmentsMessage(String username, MessageEmbed message) {
    jda.getUserById(getIdByUsername(username))
        .openPrivateChannel()
        .queue(
            (channel) -> {
              channel.sendMessageEmbeds(message).queue();
            });
  }

  /**
   * Sends a message with embedded content and attachments using an EmbedBuilder to a user's private
   * channel.
   *
   * @param username The username of the recipient.
   * @param embedBuilder The EmbedBuilder containing the message's embedded content.
   */
  @Override
  public void sendAttachmentsMessage(String username, EmbedBuilder embedBuilder) {
    jda.getUserById(getIdByUsername(username))
        .openPrivateChannel()
        .queue(
            (channel) -> {
              discordMessagingService.splitAndSendEmbed(
                  channel.getId(), embedBuilder, buttonWithEmbed);
            });
  }

  /**
   * Send attachment message with messageText to channel.
   *
   * @param channelName Name of channel
   * @param messageText Text of message
   */
  @Override
  public void sendMessageToConversation(String channelName, String messageText) {
    jda.getTextChannelById(getIdByChannelName(channelName)).sendMessage(messageText).queue();
  }

  /**
   * Sends a message with embedded content to a specific text channel in a conversation.
   *
   * @param channelName The name of the target text channel.
   * @param message The MessageEmbed content to be sent.
   */
  @Override
  public void sendBlockMessageToConversation(String channelName, MessageEmbed message) {
    jda.getTextChannelById(getIdByChannelName(channelName)).sendMessageEmbeds(message);
  }

  /**
   * Sends a message with embedded content using an EmbedBuilder to a specific text channel in a
   * conversation.
   *
   * @param channelName The name of the target text channel.
   * @param embedBuilder The EmbedBuilder containing the message's embedded content.
   */
  @Override
  public void sendBlockMessageToConversation(String channelName, EmbedBuilder embedBuilder) {
    TextChannel textChannel = jda.getTextChannelById(getIdByChannelName(channelName));
    discordMessagingService.splitAndSendEmbed(textChannel.getId(), embedBuilder, buttonWithEmbed);
  }

  /**
   * Get channel by Discord`s channelName.
   *
   * @param channelName Discord`s channelName
   * @return channelName of Channel
   */
  @Override
  public String getIdByChannelName(String channelName) {
    TextChannel channel =
        jda.getTextChannels().stream()
            .filter(textChannel -> textChannel.getName().equals(channelName))
            .findFirst()
            .get();
    String channelId = channel.getId();
    return channelId;
  }

  @Override
  public void sendAnnouncement(String message) {
    throw new NotImplementedException("This functionality is not implemented in this release.");
  }

  /**
   * Get channel by Discord`s id.
   *
   * @param id Discord`s id
   * @return channelName of Channel
   */
  public String getChannelById(String id) {
    TextChannel textChannel = jda.getTextChannelById(id);
    return textChannel.getName();
  }

  @Override
  public String getUserById(String id) {
    return jda.getUserById(id).getName();
  }

  /**
   * Get user by Discord`s id.
   *
   * @param id Slack`s id
   * @return realName of User
   */
  @Override
  public String getIdByUser(String id) {
    User user = jda.getUserById(id);
    return user.getName();
  }

  /**
   * Get user by Discord`s username.
   *
   * @param username Discord`s username
   * @return realName of User
   */
  @Override
  public String getIdByUsername(String username) {
    User user = jda.getUsers().stream().filter(u -> u.getName().equals(username)).findFirst().get();
    return user.getId();
  }

  /**
   * Get all Discord`s user.
   *
   * @return Set of users.
   */
  @Override
  public Set<ServiceUser> getAllUsers() {
    Set<ServiceUser> users =
        jda.getUsers().stream().map(ServiceUser::from).collect(Collectors.toSet());
    return users;
  }

  /**
   * Get id with real name.
   *
   * @return map key id, value real name
   */
  @Override
  public Map<String, String> getIdWithName() {
    return getAllUsers().stream()
        .filter(u -> u.getName() != null)
        .collect(Collectors.toMap(user -> user.getId(), user -> user.getName()));
  }
}
