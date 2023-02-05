# java-was-2022
Java Web Application Server 2022


## 프로젝트 정보 

이 프로젝트는 우아한 테크코스 박재성님의 허가를 받아 https://github.com/woowacourse/jwp-was 
를 참고하여 작성되었습니다.

## 간략한 코드 구조
1. webserver/WebServer에서 각 요청마다 Socket 객체 생성 및 ClientRequestThread 생성
2. ClientRequestThread에서 Enum 객체인 HttpMethod를 통해 해당 메소드와 URL에 해당하는 handler 실행시켜 Response 객체 생성
3. 생성한 Response 객체를 기반으로 HttpResponse에서 DataOutputStream을 이용해 flush

## 학습한 내용
<details>
<summary>DNS의 간략한 동작 방법</summary>
<div markdown="1">

1. 검색 창에 www.naver.com 입력 후 엔터 탁
2. 브라우저와 운영체제는 먼저 www.naver.com 의 IP 주소가 캐싱되어 있는지 체크
3. 캐싱되어 있지 않다면 Resolver를 통해 Root DNS 서버의 IP 주소를 획득
    1. Resolver는 주로 ISP(Internet Service Provider, ex. KT, SKT, U+..)로 Root DNS 서버의 주소를 알고 있음
4.  Root를 통해  Resolver로 Top-level DNS 서버의 주소를 획득
    1. 2가지 방식 존재
        1. Root가 직접 하위 서버에 쿼리를 날려 최종 목표 IP 주소를 Resolver에게 전달하는 방식
            1. Root 서버는 전 세계 13개 뿐이므로 많은 부하가 발생 - 이 방법은 사용 되지 않음
        2. Resolver가 각 서버에서 반환하는 주소로 직접 쿼리를 날려 최종 IP 주소 획득 - 현재 사용하는 방법, 즉 아래 설명된 방법
5. Top-level DNS 서버를 통해 다시 Resolver로 authoritative name server 주소 획득
6. Authoritative name server를 통해 www.naver.com의 IP 주소 획득 가능
    1. 일반적으로 한 Domain Name의 IP를 여러 authoritative name server가 가지고 있는 형태 (in case of failure)
7. 획득한 IP 주소를 이용해 사용자 request 전송 및 IP 주소 캐싱

</div>
</details>

<details>
<summary>클라이언트의 요청에 따른 웹 서버의 동작과 웹 서버의 응답에 따른 브라우저의 동작</summary>
<div markdown="1">
 
* 클라이언트가 주소 창에 localhost:8080/index.html이라는 URI를 입력
  * URI = URL + URN, URL은 localhost:8080 - resource의 위치, URN은 index.html - resource의 이름을 의미
  * 즉 URI는 URL과 URN을 통합한 것으로 특정 위치의 특정 파일을 의미, ex. localhost:8080의 index.html
* 웹 서버는 요청을 받고, 해당 위치에 파일이 존재하면 응답의 body에 해당 파일을 바이트로 변환해 첨부함
* 클라이언트는 body에 첨부된 파일을 받은 후, 만약 html 파일이 참조하는 css, js 등의 파일이 있는 경우, 해당 경로로 다시 웹 서버에 요청을 전송
* 웹 서버는 요청을 받고, 다시 요청에 명시된 경로에 있는 파일을 반환 -> 즉 한 개의 html 파일을 렌더링 하기 위해 여러 개의 요청을 처리함
* 클라이언트는, 만약 요청한 자원을 받지 못하면 서버로 몇 차례 반복적으로 자원을 요청함(브라우저의 기능)

* 웹 서버가 반환하는 response status에 따른 웹 브라우저는 동적으로 반응함
  * 예를 들어, response status 302 FOUND는 요청한 resource가 Location 헤더에 명시된 위치로 이동됐음을 의미함. 따라서 웹 브라우저는 자동으로 웹 서버에 Location에 명시된 위치로 다시 요청을 전송함

</div>
</details>

<details>
<summary>쿠키와 세션 + HTTP/1.1, HTTP/2.0</summary>
<div markdown="1">

* 쿠키는 클라이언트가 매 요청마다 서버로 전송해야 하는 정보를 파일의 형태로 기록한 것
* 쿠키 생성의 주체는 서버로, response header에 Set-Cookie라는 항목에 key-value 값을 넣어 해당 값으로 쿠키를 생성하라고 클라이언트에게 지시함 
    * 이후, 클라이언트는 생성한 쿠키를 매 요청마다 request header에 Cookie라는 항목에 첨부해 서버로 전송함
    * 쿠키는 주로 브라우저에 의해 저장되고 관리됨(주로 SQLite 사용)
* 세션의 경우, 클라이언트와 서버 간의 논리적 연결
    * 세션은 서버와 연결된 클라이언트 수 만큼 생성됨 
    * 세션을 이용해 stateless한 http 프로토콜을 stateful한 것처럼 보이게 할 수 있음
    * 주로 쿠키를 세션과 함께 이용
        * 주로 In-memory 기반 DB에 필요한 정보를 포함한 세션을 저장하고, 쿠키에 세션 ID를 담아 매 요청마다 클라이언트에 대한 세션이 존재하는지 확인하는 식으로 stateful한 동작 구조 
* HTTP/1.1은 pipeline, HTTP/2.0은 병렬처리를 지원함
    * HTTP/1.0은 모든 요소(html, css, js..)가 모여야 렌더링함
    * pipeline은 모두 모이지 않아도 순차적으로 렌더링하기 때문에 더 효율적
    * 병렬처리는 렌더링하기 위해 필요한 요소를 동시에 요청할 수 있음

</div>
</details>


<details>
<summary>JVM</summary>
<div markdown="1">

* 바이트 코드로 변환된 자바 프로그램을 실행하는 가상 머신
* JVM 자체는 지키도록 권장되는 specification, 실제로는 구현하기 나름
* JDK(Java Development Kit), JRE(Java Runtime Environment)와 한 세트, 별도로 설치하거나 그러지 않음
  * JDK: 개발을 위한 tool, 개발자가 사용
  * JRE: 자바 프로그램 실행을 위한 환경 제공
* 자바가 어느 환경에서든 실행될 수 있게 하는 핵심 기술(WORA - write once run anywhere)
  * C++의 경우, 실행되는 환경에 따라 컴파일되기 때문에 윈도우에서 컴파일한 결과물은 Linux에서 실행되지 않을 수 있음
  * 자바의 경우, 컴파일 시 바로 CPU가 실행할 수 있는 기계어로 변환되지 않고 우선적으로 JVM에 의해 바이트코드로 변환됨
  * JVM은 바이트코드를 CPU가 실행할 수 있는 기계어로 변환함
  * 이 과정을 통해 자바코드(바이트코드)는 JVM이 설치되어 있는 모든 환경에서 실행될 수 있음 - platform independent
  * 자바는 platform independent 하지만 JVM은 당연히 운영체제에 따라 여러가지 버전이 존재해야 함 
* JVM은 메모리에서 돌아가고, 하나의 자바 프로세스 당 하나의 JVM이 존재
* JVM은 non-daemon thread가 모두 종료되면 메모리에서 사라짐
  * JVM daemon thread는 백그라운드에서 돌아가는 우선 순위가 낮은 스레드로, 사용자의 애플리케이션을 보조하는 역할을 수행
  * 대표적으로 GC
  * 일반 스레드가 모두 종료되면 JVM이 할당 해제되는 것과 함께 daemon thread는 강제로 종료됨

</div>
</details>

<details>
<summary>JVM 구성</summary>
<div markdown="1">

* Class Loader
  * 3가지 작업 수행 - Loading, Linking, Initialization
    * Loading - 컴파일한 바이트 코드(.class)를 읽고 아래(binary) 데이터를 생성해 Runtime Data Area의 method area에 저장함
      * load한 클래스와 근접 부모 클래스의 FQCN(Fully Qualified Class Name - 패키지 경로를 포함한 클래스 이름)
      * load한 클래스가 클래스, 인터페이스, enum 중 어떤 것인지에 대한 정보
      * 제어자, 변수, 메소드에 대한 정보
      * 3가지 class loader
        * Boostrap class loader 
          * 모든 class loader의 부모
          * jre/lib/rt.jar(runtime java archive)에서 JVM을 구동하는데 필수적인 JDK 클래스 파일을 로딩함
          * 네이티브 언어로 구현됨
        * Extension class loader
          * jre/lib/ext의 클래스 파일을 로딩(TODO: 자세히 알아보기)
        * Application(System) class loader
          * application classpath의 클래스 파일을 로딩함
          * 간단히 말해서 개발자가 작성한 자바 코드의 클래스 파일을 로딩함
        * 4가지 Principle
          * Delegate Hierarchy Principle
            * 클래스 A를 로딩할 때, Application class loader에서 시작해 상위 class loader로 위임(Class -> Extension -> Bootstrap)
            * 최상위 class loader부터 클래스를 찾음 -> BootStrap에서 못 찾으면 Extension으로, 없으면 Class로, 최하위에서도 못 찾으면 ClassNotFoundException
            * Visibility, Uniquenessprinciple을 만족시키기 위함
          * Visibility Principle - 상위 class loader는 하위 class loader가 load한 클래스를 볼 수 없음, 반대는 가능
          * Uniqueness Principle - 상위 class loader가 load한 클래스를 하위 class loader가 중복으로 load 하지 말아야 함
          * No Unloading Principle - class loader는 load한 클래스를 unload할 수 없음, 대신 현재 class loader를 없애고 새롭게 생성은 가능
  * Linking
    * 3가지 단계
      * Verification
        * .class 파일의 유효성을 확인함
          * 코드가 Java Specification대로 작성되었는지?
          * JVM Specification에 맞는 컴파일러에 의해 생성되었는지?
        * class load process 중 제일 많은 시간 소요
      * Preparation
        * static 변수를 위한 메모리 공간을 할당하고 기본 값으로 초기화함
          * '기본 값'으로 초기화 함 -> static int a = 5;일때 5가 아니라 int의 기본 값인 0으로 초기화
      * Resolution
        * Symbolic reference를 method area에 있는 실제 주소로 변경
          * Symbolic reference - .class 파일에서 참조하고 있는 클래스의 이름만을 지칭하는 것, JVM에 올라가면 단순히 이름만 지칭하고 있는 참조에서 참조하고 있는 객체의 주소값으로 변경
  * Initialization
    * static 변수가 코드에서 정의한 값으로 초기화되고, static{} 블록 안의 코드가 실행됨

* JVM Memory
  * Method Area
  * Heap
  * Stack
  * PC Register
  * Native Method Stack

</div>
</details>


