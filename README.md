# ResourceAgent

서버 데이터를 수집하는 프로세스.
Os 정보 / Resource 정보 / Process(top) 정보 들을 수집한다.
각각 쓰레드로 동작하며, Kafka로 데이터를 전송한다.
