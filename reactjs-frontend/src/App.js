import './simple-grid.css';
import './App.css';

import React, {useEffect, useState} from "react"
import {ReactTerminal} from "react-terminal";
import axios, {all} from "axios";

let websocket = null
let allItems = []

function addItem(objectToCreate) {

    let split = objectToCreate.split(" ");
    let clazz = {
        name: split[1],
        attributes: split.slice(2, split.length + 1)
    }
    console.log("initial items " + allItems.length)
    allItems.push(clazz)
}

const startWebSockets = (setItems, setServerMessage) => {
    const websocket = new WebSocket('ws://localhost:8080/code-generator/new-generation/vlad');

    websocket.onopen = () => {
        console.log('connected');
    }

    websocket.onmessage = (event) => {
        if(event.data === "ok") {
            return
        }
        addItem(event.data)
        setServerMessage(event.data + " " + new Date())
        setItems(allItems)
    }

    websocket.onclose = () => {
        console.log("Closing websocket")
    }
    return websocket
}


function App() {

    const [items, setItems] = useState(allItems)
    const [serverMessage, setServerMessage] = useState("")

    // useEffect(() => {
    //     setItems(allItems)
    //     console.log("in useeffect " + allItems.length)
    // }, [allItems]);

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

    // Define commands here
    const commands = {
        whoami: "jzeratul",

        healthcheck: () => {
            let con = axios.get(`http://localhost:8080/healthcheck`)
            con.then(
                (response) => {
                    setServerMessage("healthcheck ok")
                },
                (error) => {
                    setServerMessage("healthcheck failed")
                })
        },
        create: (objectToCreate) => {
            if(websocket === null) {
                websocket = startWebSockets(setItems, setServerMessage)
            }
            sendMessage(websocket, "create " + objectToCreate)
            // return 'creating item...'
        }
    };
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
                            themeBGColor: "#000000", themeColor: "#00FF00", themePromptColor: "#FFFFFF"
                        }
                    }}
                    theme="my-custom-theme"y
                />
            </div>
            <div className="container m-t-100">
                <div className="row">
                    {items.map(function (item, rowIdx) {
                        return <div key={rowIdx} className="col-3-sm card">
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
                <p>$>   {serverMessage}</p>
            </div>
        </>;
}

export default App;
