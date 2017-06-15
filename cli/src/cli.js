import vorpal from 'vorpal'
import { words } from 'lodash'
import { connect } from 'net'
import { Message } from './Message'

export const cli = vorpal()

let username
let host = 'localhost'
let port = 8080
let server
let prevCommand

cli
  .delimiter(cli.chalk['yellow']('ftd~$'))

cli
  .mode('connect <username> [host] [port]')
  .delimiter(cli.chalk['green']('connected>'))
  .init(function (args, callback) {
    username = args.username
    // Check if there is a host parameter passed
    if (args.host) host = args.host
    // Check if there is a port parameter passed
    if (args.port) port = args.port
    server = connect({ host, port }, () => {
      server.write(new Message({ username, command: 'connect' }).toJSON() + '\n')
      callback()
    })
    // Change output color based on command
    server.on('data', (buffer) => {
      // Store messsage to send to server
      const mess = Message.fromJSON(buffer)
      let color
      switch (mess.command) {
        case 'echo':
          color = 'white'
          break
        case 'broadcast':
          color = 'red'
          break
        case 'users':
          color = 'blue'
          break
        case 'connect':
          color = 'green'
          break
        case 'disconnect':
          color = 'gray'
          break
        default:
          color = 'magenta'
          break
      }
      this.log(cli.chalk[color](mess.toString()))
    })

    server.on('end', () => {
      cli.exec('exit')
    })
  })
  .action(function (input, callback) {
    const [ command, ...rest ] = words(input, /[:;()\\/@.'\w]+/g)
    const contents = rest.join(' ')
    if (command === 'disconnect') {
      server.end(new Message({ username, command }).toJSON() + '\n')
    } else if (command === 'echo') {
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
      prevCommand = command
    } else if (command === 'broadcast') {
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
      prevCommand = command
    } else if (command === 'users') {
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
      prevCommand = command
    } else if (command.charAt(0) === '@') {
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
      prevCommand = command
    } else if (prevCommand) {
      const newCont = command + ' ' + contents
      server.write(new Message({ username, command: `${prevCommand}`, contents: `${newCont}` }).toJSON() + '\n')
    } else {
      this.log(`Command <${command}> was not recognized. Please enter a valid command`)
    }

    callback()
  })
