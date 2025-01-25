import { SignalStrength } from 'elsapiens-signal-strength';

window.testEcho = () => {
    const inputValue = document.getElementById("echoInput").value;
    SignalStrength.echo({ value: inputValue })
}
