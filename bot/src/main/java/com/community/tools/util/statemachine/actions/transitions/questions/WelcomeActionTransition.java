package com.community.tools.util.statemachine.actions.transitions.questions;

import com.community.tools.model.Messages;
import com.community.tools.service.MessageConstructor;
import com.community.tools.service.MessageService;
import com.community.tools.service.payload.SimplePayload;
import com.community.tools.util.statemachine.Event;
import com.community.tools.util.statemachine.State;
import com.community.tools.util.statemachine.actions.Transition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.annotation.WithStateMachine;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

@WithStateMachine
public class WelcomeActionTransition implements Transition {

  @Autowired
  private MessageService messageService;

  @Autowired
  private MessageConstructor messageConstructor;

  @Autowired
  private Action<State, Event> errorAction;

  @Override
  public void execute(StateContext<State, Event> stateContext) {
    SimplePayload payload = (SimplePayload) stateContext.getExtendedState().getVariables()
        .get("dataPayload");
    String id = payload.getId();
    messageService.sendBlocksMessage(
        messageService.getUserById(id),
        messageConstructor.createMessageAboutRules(
            Messages.MESSAGE_ABOUT_RULES_1,
            Messages.MESSAGE_ABOUT_RULES_2,
            Messages.MESSAGE_ABOUT_RULES_3,
            Messages.MESSAGE_ABOUT_RULES_4));
  }

  @Override
  public void configure(
      StateMachineTransitionConfigurer<State, Event> transitions) throws Exception {
    transitions
        .withExternal()
        .source(State.NEW_USER)
        .target(State.RULES)
        .event(Event.GET_RULES)
        .action(this, errorAction);
  }
}
