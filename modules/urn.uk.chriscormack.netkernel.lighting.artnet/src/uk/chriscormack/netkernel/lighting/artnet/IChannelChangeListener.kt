package uk.chriscormack.netkernel.lighting.artnet

interface IChannelChangeListener {
    fun channelsChanged(changes: Map<Int, Int>)
}
