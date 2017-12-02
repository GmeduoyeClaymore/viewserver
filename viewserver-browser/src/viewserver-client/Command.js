export default class Command {
  constructor(command, data) {
    this.id = -1;
    this.handler = undefined;
    this.command = command;
    this.data = data;
    this.continuous = false;
  }
}
