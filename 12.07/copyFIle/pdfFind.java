import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class pdfFind {
    public static void main(String[] args) throws InterruptedException, UnsupportedEncodingException {
        if (args.length < 3) {
            System.out.println("Please input all arguments");
            System.exit(0);
        }

        String filepath = "C:\\Users\\user-1\\Desktop\\JAVA\\JB";
        String signature = "%PDF-";
        String fileType = "PDF document";
        if(checkPDF(signature,"11.pdf")){
            System.out.println("PDF документ");
        }else {
            System.out.println(" Unknown file type");
        }

//        Unknown file type

        legacyWrite();
        legacyRead();
        streamLegacyWrite();
        newReadWrite();
        System.out.println(checkPNG());
        bufferStreamReadWrite();
    }
    //особенность у легаси в том что запись идет побайтно
    static void legacyWrite(){
        try{
            OutputStream os = new FileOutputStream("1.txt");
            int byt = 0x20;
            int byt1 = 0x32;
            os.write(byt);
            os.write(byt1);
        } catch (IOException ex){
            ex.printStackTrace();
        }

    }
    static void legacyRead(){
        try{
            InputStream is = new FileInputStream("1.txt");
            int byteRead;
            while((byteRead = is.read()) != - 1){
                System.out.println(byteRead);
            }
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

    //копирование байтовым массивом
    static void streamLegacyWrite(){
        try{
            InputStream is = new FileInputStream("testForCopy.txt");
            OutputStream os = new FileOutputStream("copiedFile.txt");

            //узнать размер файла
            long fileSize = new File("testForCopy.txt").length();
            byte[] content = new byte[512];
            int readResult;
            while(is.read(content) != -1){
                os.write(content);
                content = new byte[512];
            }
        } catch (IOException ex){
            ex.printStackTrace();
        }
    }
    //проверить является ли файл PNG
    static boolean checkPNG(){
        int[] pngSignature = {137, 80, 78, 71, 13, 10, 26, 10};
        String inputFile = "11.3.png";
        try{
            InputStream is = new FileInputStream(inputFile);
            // нельзя сразу считать все и проверить на равенство
            //т.к. 137 выходит за диапазон байта а
            // другие типы функция не читает
            int []inputFileHeader = new int[8];

            for (int i = 0;i < 8;i++){
                inputFileHeader[i] = is.read();
                if (pngSignature[i]!=inputFileHeader[i]){
                    return false;
                }

            }



        } catch(IOException ex) {
            ex.printStackTrace();
        }
        return true;
    }
    //написать общую функцию по считвыанию мб?
    //куда передается число  сигнатура
    //и по ней вычисляетя длина
    static boolean checkPDF(String signature,String inputFile) throws UnsupportedEncodingException {
        char []a = signature.toCharArray();
        try
        {
            InputStream is = new FileInputStream(inputFile);
            int []fileHeader = new int[signature.length()];

            for (int i = 0;i < signature.length();i++){
                fileHeader[i] = is.read();
                if (a[i] != fileHeader[i]){
                    return false;
                }
            }
        } catch (IOException ex) {
            return false;
        }

        return true;
    }
    //bufferStream как?
    static void bufferStreamReadWrite(){
        try{
            InputStream inputStream = new BufferedInputStream(new
                    FileInputStream("1.pdf"));
            OutputStream os = new BufferedOutputStream(new
                    FileOutputStream("2.pdf"));

            byte[] buffer = new byte[8192];
            long fileSize = new File("1.pdf").length();
            long size = 0;
            //почему файл получился больше?
            while(inputStream.read(buffer) != -1){
                os.write(buffer);
                size += buffer.length;
                System.out.print("\r"+size +"/"+ (fileSize));
                buffer = new byte[8192];
            }

        } catch (IOException ex){
            ex.printStackTrace();
        }
    }
    //новое апи для работы с файловым вводом выводом
    static void newReadWrite(){
        try {
            long start = System.currentTimeMillis();

            byte[] allBytes = Files.readAllBytes(Paths.get("1.pdf"));
            Files.write(Paths.get("2.pdf"), allBytes);

            long end = System.currentTimeMillis();
            System.out.println("Copied in " + (end - start) + " ms");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

//    static void proggress() throws InterruptedException {
//        int progress = 10;
//        for(progress = 10; progress<=100; progress+=10) {
//            System.out.print("\rPercent = " + (progress) + "%");
//            Thread.sleep(3000);
//        }
//    }

}
