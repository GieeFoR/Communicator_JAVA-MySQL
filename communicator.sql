-- phpMyAdmin SQL Dump
-- version 5.0.3
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Czas generowania: 17 Mar 2021, 12:10
-- Wersja serwera: 10.4.14-MariaDB
-- Wersja PHP: 7.2.34

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Baza danych: `communicator`
--
CREATE DATABASE IF NOT EXISTS `communicator` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `communicator`;

-- --------------------------------------------------------

--
-- Struktura tabeli dla tabeli `conversation`
--

DROP TABLE IF EXISTS `conversation`;
CREATE TABLE `conversation` (
  `CONVERSATION_ID` int(10) UNSIGNED NOT NULL,
  `FOUNDER_ID` int(10) UNSIGNED NOT NULL,
  `NAME` varchar(32) NOT NULL,
  `DESCRIPTION` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Struktura tabeli dla tabeli `message`
--

DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
  `MESSAGE_ID` int(10) UNSIGNED NOT NULL,
  `AUTHOR_ID` int(10) UNSIGNED NOT NULL,
  `CONVERSATION_ID` int(10) UNSIGNED NOT NULL,
  `CONTENT` text NOT NULL,
  `SEND_DATE` date NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Struktura tabeli dla tabeli `user`
--

DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `USER_ID` int(10) UNSIGNED NOT NULL,
  `NUMBER` int(10) UNSIGNED NOT NULL,
  `NAME` varchar(32) NOT NULL,
  `SURNAME` varchar(64) NOT NULL,
  `USERNAME` varchar(32) NOT NULL,
  `PASSWORD` varchar(64) NOT NULL,
  `CREATION_DATE` date NOT NULL,
  `E_MAIL` varchar(32) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Zrzut danych tabeli `user`
--

INSERT INTO `user` (`USER_ID`, `NUMBER`, `NAME`, `SURNAME`, `USERNAME`, `PASSWORD`, `CREATION_DATE`, `E_MAIL`) VALUES
(13, 1000, 'user1', 'user1', 'user1', 'pass', '2021-01-26', 'user1@example.com'),
(14, 1001, 'user2', 'user2', 'user2', 'pass', '2021-01-26', 'user2@example.com'),

-- --------------------------------------------------------

--
-- Struktura tabeli dla tabeli `user_conversation`
--

DROP TABLE IF EXISTS `user_conversation`;
CREATE TABLE `user_conversation` (
  `USER_ID` int(10) UNSIGNED NOT NULL,
  `CONVERSATION_ID` int(10) UNSIGNED NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Indeksy dla zrzutów tabel
--

--
-- Indeksy dla tabeli `conversation`
--
ALTER TABLE `conversation`
  ADD PRIMARY KEY (`CONVERSATION_ID`),
  ADD KEY `FK_CONVERSATION_FOUNDER` (`FOUNDER_ID`);

--
-- Indeksy dla tabeli `message`
--
ALTER TABLE `message`
  ADD PRIMARY KEY (`MESSAGE_ID`),
  ADD KEY `FK_MESSAGE_AUTHOR` (`AUTHOR_ID`),
  ADD KEY `FK_MESSAGE_CONVERSATION` (`CONVERSATION_ID`);

--
-- Indeksy dla tabeli `user`
--
ALTER TABLE `user`
  ADD PRIMARY KEY (`USER_ID`),
  ADD UNIQUE KEY `USERNAME` (`USERNAME`,`E_MAIL`);

--
-- Indeksy dla tabeli `user_conversation`
--
ALTER TABLE `user_conversation`
  ADD PRIMARY KEY (`USER_ID`,`CONVERSATION_ID`),
  ADD KEY `FK_USER_CONVERSATION_CONVERSATION` (`CONVERSATION_ID`);

--
-- AUTO_INCREMENT dla zrzuconych tabel
--

--
-- AUTO_INCREMENT dla tabeli `conversation`
--
ALTER TABLE `conversation`
  MODIFY `CONVERSATION_ID` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=54;

--
-- AUTO_INCREMENT dla tabeli `message`
--
ALTER TABLE `message`
  MODIFY `MESSAGE_ID` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=280;

--
-- AUTO_INCREMENT dla tabeli `user`
--
ALTER TABLE `user`
  MODIFY `USER_ID` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=17;

--
-- Ograniczenia dla zrzutów tabel
--

--
-- Ograniczenia dla tabeli `conversation`
--
ALTER TABLE `conversation`
  ADD CONSTRAINT `FK_CONVERSATION_FOUNDER` FOREIGN KEY (`FOUNDER_ID`) REFERENCES `user` (`USER_ID`);

--
-- Ograniczenia dla tabeli `message`
--
ALTER TABLE `message`
  ADD CONSTRAINT `FK_MESSAGE_AUTHOR` FOREIGN KEY (`AUTHOR_ID`) REFERENCES `user` (`USER_ID`),
  ADD CONSTRAINT `FK_MESSAGE_CONVERSATION` FOREIGN KEY (`CONVERSATION_ID`) REFERENCES `conversation` (`CONVERSATION_ID`);

--
-- Ograniczenia dla tabeli `user_conversation`
--
ALTER TABLE `user_conversation`
  ADD CONSTRAINT `FK_USER_CONVERSATION_CONVERSATION` FOREIGN KEY (`CONVERSATION_ID`) REFERENCES `conversation` (`CONVERSATION_ID`),
  ADD CONSTRAINT `FK_USER_CONVERSATION_USER` FOREIGN KEY (`USER_ID`) REFERENCES `user` (`USER_ID`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
