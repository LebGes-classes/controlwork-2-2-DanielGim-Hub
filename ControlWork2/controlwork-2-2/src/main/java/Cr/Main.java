package Cr;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        String inputFilePath = "data.txt";
        List<String> lines = new ArrayList<>();

        Map<BroadcastsTime, List<Program>> schedule = new TreeMap<>();
        List<Program> allPrograms = new ArrayList<>();
        String currentChannel = "";

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.startsWith("#")) {
                currentChannel = line.substring(1).trim();
            } else if (!line.isEmpty()) {
                try {
                    BroadcastsTime time = new BroadcastsTime(line);
                    String title = lines.get(++i).trim();
                    Program program = new Program(currentChannel, time, title);

                    schedule.computeIfAbsent(time, k -> new ArrayList<>()).add(program);
                    allPrograms.add(program);
                } catch (IllegalArgumentException e) {
                    System.err.println("Ошибка обработки строки: " + line + ". " + e.getMessage());
                }
            }
        }

        try (BufferedReader br = new BufferedReader(new FileReader(inputFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        allPrograms.sort(Comparator.comparing(Program::getChannel).thenComparing(Program::getTime));

        // Вывод всех программ в порядке возрастания канала и времени показа
        System.out.println("Все программы в порядке возрастания канала и времени показа:");
        allPrograms.forEach(System.out::println);

        // Заданное время для поиска текущих программ
        BroadcastsTime searchTime = new BroadcastsTime("17:30");
        List<Program> currentPrograms = getProgramsCurrentlyAiring(allPrograms, searchTime);

        System.out.println("\nПрограммы, которые идут в " + searchTime + ":");
        currentPrograms.forEach(System.out::println);

        // Поиск всех программ по некоторому названию
        String searchTitle = "ПОДКАСТ.ЛАБ. 20 лет спустя";
        List<Program> programsByTitle = getProgramsByTitle(allPrograms, searchTitle);

        System.out.println("\nПрограммы по названию \"" + searchTitle + "\":");
        programsByTitle.forEach(System.out::println);

        // Поиск всех программ определенного канала, которые идут сейчас
        String searchChannel = "Первый";
        List<Program> currentProgramsByChannel = getProgramsByChannelAndTime(allPrograms, searchChannel, searchTime);

        System.out.println("\nПрограммы канала \"" + searchChannel + "\", которые идут в " + searchTime + ":");
        currentProgramsByChannel.forEach(System.out::println);

        // Поиск всех программ определенного канала, которые будут идти в некотором промежутке времени
        BroadcastsTime startTime = new BroadcastsTime("04:00");
        BroadcastsTime endTime = new BroadcastsTime("06:00");
        List<Program> programsInTimeRange = getProgramsByChannelAndTimeRange(allPrograms, searchChannel, startTime, endTime);

        System.out.println("\nПрограммы канала \"" + searchChannel + "\", которые будут идти с " + startTime + " до " + endTime + ":");
        programsInTimeRange.forEach(System.out::println);

        // Запись отсортированных данных в Excel файл
        String outputFilePath = "SortedList.xlsx";

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("TV");
            int rowNum = 0;

            for (Program program : allPrograms) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(program.getChannel());
                row.createCell(1).setCellValue(program.getTime().toString());
                row.createCell(2).setCellValue(program.getTitle());
            }

            try (var fileOut = Files.newOutputStream(Paths.get(outputFilePath))) {
                workbook.write(fileOut);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<Program> getProgramsCurrentlyAiring(List<Program> programs, BroadcastsTime now) {
        List<Program> currentPrograms = new ArrayList<>();
        for (int i = 0; i < programs.size(); i++) {
            Program currentProgram = programs.get(i);
            BroadcastsTime startTime = currentProgram.getTime();
            BroadcastsTime endTime = (i + 1 < programs.size() && programs.get(i + 1).getChannel().equals(currentProgram.getChannel())) ?
                    programs.get(i + 1).getTime() : new BroadcastsTime("23:59");

            if (now.between(startTime, endTime)) {
                currentPrograms.add(currentProgram);
            }
        }
        return currentPrograms;
    }

    private static List<Program> getProgramsByTitle(List<Program> programs, String title) {
        return programs.stream()
                .filter(program -> program.getTitle().equalsIgnoreCase(title))
                .collect(Collectors.toList());
    }

    private static List<Program> getProgramsByChannelAndTime(List<Program> programs, String channel, BroadcastsTime time) {
        return programs.stream()
                .filter(program -> program.getChannel().equalsIgnoreCase(channel) && time.between(program.getTime(),
                        (programs.indexOf(program) + 1 < programs.size() && programs.get(programs.indexOf(program) + 1).getChannel().equals(channel)) ?
                                programs.get(programs.indexOf(program) + 1).getTime() : new BroadcastsTime("23:59")))
                .collect(Collectors.toList());
    }

    private static List<Program> getProgramsByChannelAndTimeRange(List<Program> programs, String channel, BroadcastsTime startTime, BroadcastsTime endTime) {
        return programs.stream()
                .filter(program -> program.getChannel().equalsIgnoreCase(channel) && program.getTime().between(startTime, endTime))
                .collect(Collectors.toList());
    }
}
