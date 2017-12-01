export default class Logger {
  static LogLevels = {
    Error: {
      level: 0,
      label: 'Error'
    },
    Warning: {
      level: 1,
      label: 'Warning'
    },
    Info: {
      level: 2,
      label: 'Info'
    },
    Debug: {
      level: 3,
      label: 'Debug'
    },
    Fine: {
      level: 4,
      label: 'Fine'
    }
  };

  static LogLevel = 1;

  static log(logLevel, message, json){
    if (logLevel.level <= Logger.LogLevel) {
      const dateTime = new Date();
      message = '[' + logLevel.label + ' ' +
        dateTime.getHours() + ':' + dateTime.getMinutes() + ':' + dateTime.getSeconds() + ':' + dateTime.getMilliseconds() +
        '] ' + message;

      Logger.output(logLevel, message);

      if (json) {
        Logger.output(logLevel, json);
      }
    }
  }

  static output(logLevel, message) {
    if (logLevel === Logger.LogLevels.Error) {
      console.error(message);
    } else {
      console.log(message);
    }
  }

  static debug(message, json){
    Logger.log(Logger.LogLevels.Debug, message, json);
  }
  static info(message, json){
    Logger.log(Logger.LogLevels.Info, message, json);
  }
  static error(message, json){
    Logger.log(Logger.LogLevels.Error, message, json);
  }
  static warning(message, json){
    Logger.log(Logger.LogLevels.Warning, message, json);
  }
  static fine(message, json){
    Logger.log(Logger.LogLevels.Fine, message, json);
  }
}
