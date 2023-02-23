package github.mumu.rpc07_guide.protocol.compress;


import github.mumu.rpc07_guide.common.extension.SPI;

@SPI
public interface Compress {

    byte[] compress(byte[] bytes);


    byte[] decompress(byte[] bytes);
}
