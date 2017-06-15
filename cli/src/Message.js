export class Message {
  static fromJSON (buffer) {
    return new Message(JSON.parse(buffer.toString()))
  }

  constructor ({ username, command, contents, timestamp }) {
    this.username = username
    this.command = command
    this.contents = contents
    this.timestamp = timestamp
  }
// Send JSON object
  toJSON () {
    return JSON.stringify({
      username: this.username,
      command: this.command,
      contents: this.contents,
      timestamp: this.timestamp
    })
  }
// Dispay server messages
  toString () {
    let st
    switch (this.command) {
      case 'echo':
        st = `${this.timestamp} <${this.username}> (echo): ${this.contents}`
        break
      case 'broadcast':
        st = `${this.timestamp}: <${this.username}> (all): ${this.contents}`
        break
      case 'users':
        st = `${this.timestamp}: currently connected users: ${this.contents}`
        break
      case 'connect':
        if (this.contents === 'dupe') {
          st = `Username <${this.username}> is already in use. Try again.`
        } else {
          st = `${this.timestamp}: <${this.username}> has connected`
        }
        break
      case 'disconnect':
        st = `${this.timestamp}: <${this.username}> has disconnected`
        break
      default:
        st = `${this.timestamp} <${this.username}> (whisper): ${this.contents}`
        break
    }
    return st
  }
}
