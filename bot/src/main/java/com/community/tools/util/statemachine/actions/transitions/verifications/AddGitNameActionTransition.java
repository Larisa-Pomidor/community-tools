package com.community.tools.util.statemachine.actions.transitions.verifications;

import com.community.tools.model.Messages;
import com.community.tools.model.User;
import com.community.tools.repository.UserRepository;
import com.community.tools.service.MessageConstructor;
import com.community.tools.service.MessageService;
import com.community.tools.service.github.ClassroomService;
import com.community.tools.service.payload.VerificationPayload;
import com.community.tools.util.statemachine.Event;
import com.community.tools.util.statemachine.State;
import com.community.tools.util.statemachine.actions.Transition;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.kohsuke.github.GHUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.annotation.WithStateMachine;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

@NoArgsConstructor
@AllArgsConstructor
@WithStateMachine
public class AddGitNameActionTransition implements Transition {

  @Autowired
  private Action<State, Event> errorAction;

  @Value("${generalInformationChannel}")
  private String channel;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ClassroomService classroomService;

  @Autowired
  private MessageService messageService;

  @Autowired
  private MessageConstructor messageConstructor;

  @Override
  public void configure(StateMachineTransitionConfigurer<State, Event> transitions)
      throws Exception {
    transitions
        .withExternal()
        .source(State.CHECK_LOGIN)
        .target(State.GETTING_PULL_REQUEST)
        .event(Event.ADD_GIT_NAME_AND_FIRST_TASK)
        .action(this, errorAction);
  }

  @SneakyThrows
  @Override
  public void execute(StateContext<State, Event> stateContext) {
    VerificationPayload payload =
        (VerificationPayload) stateContext.getExtendedState().getVariables().get("dataPayload");
    String user = payload.getId();
    String nickname = payload.getGitNick();

    User stateEntity = userRepository.findByUserID(user).get();
    stateEntity.setGitName(nickname);
    String firstAnswer = stateEntity.getFirstAnswerAboutRules();
    String secondAnswer = stateEntity.getSecondAnswerAboutRules();
    String thirdAnswer = stateEntity.getThirdAnswerAboutRules();
    GHUser userGitLogin = new GHUser();
    try {
      classroomService.addUserToTraineesTeam(nickname);
      stateEntity.setEmail(userGitLogin.getEmail());
    } catch (Exception e) {
      messageService.sendBlocksMessage(
          messageService.getUserById(user),
          messageConstructor.createErrorWithAddingGitNameMessage(
              Messages.ERROR_WITH_ADDING_GIT_NAME));
    }
    userRepository.save(stateEntity);
    messageService.sendMessageToConversation(
        channel,
        generalInformationAboutUserToChannel(user, userGitLogin)
            + "\n"
            + sendUserAnswersToChannel(firstAnswer, secondAnswer, thirdAnswer));
    messageService.sendBlocksMessage(
        messageService.getUserById(user),
        messageConstructor.createGetFirstTaskMessage(
            Messages.CONGRATS_AVAILABLE_NICK, Messages.GET_FIRST_TASK, Messages.LINK_FIRST_TASK));
    stateContext.getExtendedState().getVariables().put("gitNick", nickname);
  }

  private String generalInformationAboutUserToChannel(String slackName, GHUser user) {
    return messageService.getUserById(slackName) + " - " + user.getLogin();
  }

  private String sendUserAnswersToChannel(
      String firstAnswer, String secondAnswer, String thirdAnswer) {
    return "Answer on questions : \n"
        + "1. " + firstAnswer + ";\n"
        + "2. " + secondAnswer + ";\n"
        + "3. " + thirdAnswer + ".";
  }
}
