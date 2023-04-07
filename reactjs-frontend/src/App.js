import './simple-grid.css';
import './App.css';

import React, {useState} from "react"
import {ReactTerminal} from "react-terminal";
import axios from "axios";

let websocket = null
let i = 0

function App(props) {
    // Define commands here
    const commands = {
        whoami: "jzeratul",

        ws: () => {
            if(websocket === null) {
                websocket = startWebSockets()
                i = 10
            }
            sendMessage(websocket, "Sending message " + i++ )
        },
        create: (objectToCreate) => {
            createDomain(objectToCreate)
            addItem(objectToCreate)
            return 'creating item...'
        },
        test: () => {
            let con = testServerConnection()
            con.then(
                (response) => {
                    setServerConnection("Connection successful" + Date())
                },
                (error) => {
                    setServerConnection("Connection failed" + Date())
                })
        }
    };

    const [items, setItems] = useState([])
    const [serverConnection, setServerConnection] = useState("")
    const [serverMessage, setServerMessage] = useState("")

    function addItem(objectToCreate) {

        let split = objectToCreate.split(" ");
        let clazz = {
            name: split[0], 
            attributes: split.slice(1, split.length + 1)
        }
        let newItems = [...items]
        newItems.push(clazz)
        setItems(newItems)
    }

    function createDomain(objectToCreate) {
        return axios.post(`http://localhost:8080/create`, {createCommand: objectToCreate})
    }

    const testServerConnection = () => {
        return axios.get(`http://localhost:8080/healthcheck`)

    }

    const startWebSockets = () => {
        const websocket = new WebSocket('ws://localhost:8080/chat/topic/userX');

        websocket.onopen = () => {
            console.log('connected');
        }

        websocket.onmessage = (event) => {
            // const data = JSON.parse(event.data);
            // console.log("oh 6 " + data)
            setServerMessage(event.data)
        }

        websocket.onclose = () => {
            console.log("Closing websocket")
        }
        return websocket
    }

    const waitForOpenConnection = (socket) => {
        return new Promise((resolve, reject) => {
            const maxNumberOfAttempts = 10
            const intervalTime = 300 //ms

            let currentAttempt = 0
            const interval = setInterval(() => {
                if (currentAttempt > maxNumberOfAttempts - 1) {
                    clearInterval(interval)
                    reject(new Error('Maximum number of attempts exceeded'))
                } else if (socket.readyState === socket.OPEN) {
                    clearInterval(interval)
                    currentAttempt = 0
                    resolve()
                }
                currentAttempt++
            }, intervalTime)
        })
    }

    const sendMessage = async (socket, msg) => {
        if (socket.readyState !== socket.OPEN) {
            try {
                await waitForOpenConnection(socket)
                socket.send(msg)
            } catch (err) { console.error(err) }
        } else {
            socket.send(msg)
        }
    }

    return <>
            <div className="max-height fixed-top">

                <ReactTerminal
                    commands={commands}
                    showControlButtons={false}
                    showControlBar={false}
                    prompt={' $root>'}
                    themes={{
                        'my-custom-theme': {
                            themeBGColor: "#272B36", themeColor: "#00FF00", themePromptColor: "#FFFFFF"
                        }
                    }}
                    theme="my-custom-theme"y
                />
            </div>
            <div className="container m-t-105">
                <div className="row">
                    {items.map(function (item, rowIdx) {
                        return <div key={rowIdx} className="col-2-sm card">
                                <div className="container">
                                    <h4>{item.name}</h4>
                                    {item.attributes.map(function (attr, attrIdx) {
                                        return (<p key={attrIdx} className="">
                                            {attr}
                                        </p>)
                                    })}
                                </div>
                            </div>
                    })}
                </div>
            </div>

            <div className="footer">
                <p>{serverMessage}</p>
                <p>{serverConnection}</p>
            </div>
        </>;
}

export default App;
