import Component from '@glimmer/component';

interface UiExerciseButtonComponentArguments {
  title?: string;
  mode?: 'completed' | 'active' | 'locked' | 'disabled';
}

export default class UiExerciseButtonComponent extends Component<
  UiExerciseButtonComponentArguments
> {
  get classes() {
    let items = ['focus:outline-none'];
    if (this.args.mode) {
      items.push(this.args.mode);
    }
    return items.join(' ');
  }
  get isDisabled() {
    return this.args.mode === 'disabled';
  }

  get isCompleted() {
    return this.args.mode === 'completed';
  }

  get isLocked() {
    return this.args.mode === 'locked';
  }

  get titleClasses() {
    if (this.args.mode === 'locked') {
      return 'title title-locked';
    }

    if (this.args.mode === 'disabled') {
      return 'title title-disabled';
    }
    return 'title';
  }

  get checkStyle() {
    return 'position: relative; bottom: 191px; right: -135px;';
  }
}
