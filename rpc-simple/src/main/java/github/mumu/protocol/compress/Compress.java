package github.mumu.protocol.compress;


import github.mumu.common.extension.SPI;

@SPI
public interface Compress {

    byte[] compress(byte[] bytes);


    byte[] decompress(byte[] bytes);
}
