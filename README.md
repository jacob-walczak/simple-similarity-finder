# Simple Similarity Finder

Lokalna aplikacja CLI do hybrydowego wyszukiwania podobnych nazw Test Case'ow z pliku CSV.

Wynik laczy:

- lexical similarity, czyli proste dopasowanie tekstowe po normalizacji,
- semantic similarity, czyli cosine similarity embeddingow,
- embeddingi generowane lokalnie przez Ollama modelem `nomic-embed-text`.

Nie ma tu REST API, ChatModel, ChatClient, RAG ani zewnetrznego vector store. Dane i embeddingi sa trzymane w RAM.

## Wymagania

- Java 21
- Maven Wrapper z repozytorium
- lokalna Ollama

## Przygotowanie Ollamy

Pobierz model embeddingowy:

```bash
ollama pull nomic-embed-text
```

Ollama musi dzialac lokalnie pod adresem skonfigurowanym w `application.yaml`, domyslnie `http://localhost:11434`.

## Konfiguracja

Najwazniejsze ustawienia sa w `src/main/resources/application.yaml`:

```yaml
similarity-finder:
  csv-path: classpath:test-cases.csv
  result-limit: 10
  semantic-weight: 0.65
  lexical-weight: 0.35
  minimum-score: 0.50
  show-score-details: true
```

- `csv-path` - lokalizacja CSV z Test Case'ami.
- `result-limit` - maksymalna liczba wynikow.
- `semantic-weight` - waga wyniku semantycznego.
- `lexical-weight` - waga wyniku leksykalnego.
- `minimum-score` - minimalny finalny wynik wymagany do pokazania rekordu.
- `show-score-details` - pokazuje skladowe `final`, `semantic`, `lexical`.

Konfiguracja Ollamy:

```yaml
spring:
  ai:
    ollama:
      base-url: http://localhost:11434
      embedding:
        model: nomic-embed-text
```

## Uruchomienie

Przez Maven Wrapper:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="sprzedaż gotówką"
```

Po zbudowaniu JAR:

```bash
./mvnw clean package
java -jar target/simple-similarity-finder.jar "sprzedaż gotówką"
```

## Przykladowy output

```text
Query: sprzedaż gotówką

Found 10 similar Test Cases:

1. TC-0001 | POS - sprzedaż gotówką
   final:    0.972
   semantic: 0.957
   lexical:  1.000

2. TC-0187 | Realizacja płatności gotówkowej w POS
   final:    0.891
   semantic: 0.934
   lexical:  0.812
```

## Jak dziala scoring

```text
finalScore =
semanticScore * semanticWeight
+ lexicalScore * lexicalWeight
```

`semanticScore` to cosine similarity pomiedzy embeddingiem query i embeddingiem nazwy Test Case'a.

`lexicalScore` uwzglednia exact normalized match, wystapienie calej frazy w nazwie oraz overlap tokenow.

## Ograniczenia MVP

- embeddingi sa liczone ponownie przy kazdym uruchomieniu,
- nie ma trwalego vector store,
- nie ma cache embeddingow,
- przy okolo 2000 rekordow wszystko jest trzymane w RAM,
- w przyszlosci mozna dodac cache embeddingow na dysku.
