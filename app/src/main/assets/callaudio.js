let localAudio = document.getElementById("local-audio")
let remoteAudio = document.getElementById("remote-audio")

localAudio.style.opacity = 0
remoteAudio.style.opacity = 0

localAudio.onplaying = () => { audio.style.opacity = 1 }
remoteAudio.onplaying = () => { audio.style.opacity = 1 }


let peer
function init(userId) {
    peer = new Peer(userId, {
        port: 443,
        path: '/'
    })

    peer.on('open', () => {
        Android.onPeerConnected()
    })

    listen()
}

let localStream
function listen() {
    peer.on('call', (call) => {

      navigator.getUserMedia({
               audio: true,
               video: false
           }, (stream) => {
               localAudio.srcObject = stream
               localStream = stream
               AndroidE.onCallConnected()
               call.answer(stream)
               call.on('stream', (remoteStream) => {
                   remoteAudio.srcObject = remoteStream
                   remoteAudio.className = "primary-audio"
                   localAudio.className = "secondary-audio"

               })

           })


    })
}

function startCall(otherUserId) {
    navigator.getUserMedia({
           audio: true,
           video: false
       }, (stream) => {

           localAudio.srcObject = stream
           localStream = stream
           AndroidE.onCallConnected()
           const call = peer.call(otherUserId, stream)
           call.on('stream', (remoteStream) => {
               remoteAudio.srcObject = remoteStream
               remoteAudio.className = "primary-audio"
               localAudio.className = "secondary-audio"
           })

       })
}

function toggleAudio(b) {
    if (b == "true") {
        localStream.getAudioTracks()[0].enabled = true
    } else {
        localStream.getAudioTracks()[0].enabled = false
    }
}

function closeStreamNow() {
    localAudio.srcObject.getTracks().forEach(track => track.stop())
    remoteAudio.srcObject.getTracks().forEach(track => track.stop())
    getAudioTracks.forEach(t => t.stop());
    localStream.getTracks().forEach(t => t.stop());
    localStream.close();
    localStream.release();
    localStream = null;

}

