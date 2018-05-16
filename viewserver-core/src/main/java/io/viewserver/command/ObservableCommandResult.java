package io.viewserver.command;

import io.viewserver.network.Command;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.ReplaySubject;

public class ObservableCommandResult extends CommandResult{

    final ReplaySubject subject = ReplaySubject.create(1);

    public ObservableCommandResult() {
        this.setListener(commandResult -> subject.onNext(commandResult));
    }

    public Observable<CommandResult> observable() {
        return subject;
    }
}
