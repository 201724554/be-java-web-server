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

* Runtime Data Area
  * Method Area
    * Runtime Constant Pool
      * string constant, numeric constant, class reference...
      * 런타임에 생성되는 static 상수 저장소
    * 메소드 정보(이름, 리턴 타입 등) + 코드
    * 멤버(필드) 변수
    * static 변수
    * JVM 안에서 공유됨
  * Heap
    * 런타임에 new 키워드로 생성된 객체 및 배열 저장
    * GC의 대상
    * JVM 안에서 공유됨
  * Stack
    * 각 메소드 호출 시 하나의 stack frame 할당됨
    * 메소드 안에서 선언되는 로컬 변수 저장
    * 메소드가 값을 리턴하거나(끝나거나) 예외가 발생할 때 stack frame 제거됨
    * 스레드 당 하나의 stack 할당
  * PC Register
    * context switch 발생 시, 어디까지 실행됐는지 process count를 저장
    * 스레드 당 하나 할당
  * Native Method Stack
    * native method 호출 시 사용되는 스택
    * native method
      * JVM이 동작하는 아키텍쳐에서 사용되는 언어로 작성된 메소드, 주로 C, C++

* Execution Engine
  * Interpreter
    * 바이트코드를 한 줄씩 읽고 기계어로 변환
    * 바이트코드 한 줄을 읽고 실행하는 건 빠르지만 전체를 읽고 실행하는 건 느림
    * 한 메소드가 여러번 호출될 경우, 매번 다시 읽고 실행하는 과정을 거침 -> JIT 컴파일러를 이용해 단점 보완
  * JIT Compiler
    * 특정 메소드가 반복적으로 호출되는 경우, 바이트코드를 컴파일해 native code로 변환함
    * 이후 반복적 호출에 native code를 실행시킴 -> interpreter가 바이트코드를 한 줄씩 읽는 것 보다 좋은 성능
    * 하지만 JIT Compiler가 바이트코드를 컴파일 하는 것보다 interpreter가 읽고 실행하는 경우가 빠른 경우가 있음 + native code는 cache에 저장됨(고비용)
    * 이런 경우, JIT Compiler는 메소드 호출 빈도를 확인하고, 일정 횟수 이상인 경우, 위 방법 사용 -> adaptive compiling
    * 4가지 구성
      * Intermediate Code Generator - intermediate code(바이트코드와 native code 사이?) 생성
      * Code Optimizer - 위에서 생성한 intermediate code optimize
      * Target Code Generator - intermediate code에서 native code 생성
      * Profiler - hotspot을 찾는 역할 수행(ex. 메소드가 여러번 호출되는 인스턴스 찾기)
  * GC
    * heap 영역에서 더 이상 사용하지 않는 객체를 메모리에서 할당 해제하는 역할 수행
    * mark and sweep 방식
      * stack 등에서 heap 영역에 생성된 객체의 레퍼런스 값을 가지고 있는데, 이를 이용해 레퍼런스 값이 가르키고 있는 객체를 마킹함
      * 마킹되지 않은 객체(unreachable)를 모두 heap 영역에서 제거
    * minor gc, major gc
      * minor gc
        * heap 공간은 eden, survivor0, survivor1, old generation으로 나뉨
        * 객체가 처음 생성될 때 age bit가 0으로 초기화되어 eden에 할당됨
        * eden 공간이 채워지면 minor gc가 동작하고, 살아남은 객체는 age bit가 1 더해져 survivor0으로 옮겨짐
        * 다시 eden 공간이 채워지면 minor gc가 동작, 살아남은 객체는 age bit가 1 더해져 survivor1로 옮겨짐 -> 0에서 1, 1에서 0, 즉 survivor 영역 중 하나는 반드시 비워진 상태
        * age bit가 특정 정도보다 커지면 해당 객체를 old generation으로 이동, old generation은 minor gc의 대상이 아님, 즉 상시 사용되는 객체를 저장함
      * major gc
        * old generation 영역이 채워지면 major gc 동작
        * old generation의 모든 객체를 대상으로 실행됨
        * 매우 비용이 큰 동작, 동작 시 모든 스레드가 정지함 -> stop the world
        * JVM을 튜닝할 때, major gc의 동작 빈도를 줄이고 minor gc가 자주 동작하도록 하는 경우가 많음
    * System.gc()로 직접 gc를 호출할 수 있지만 금기시 됨 -> 호출 시점에서 프로세스의 상태를 알 수 없음, 어떤 스레드에서 어떤 동작을 하는지는 매 순간 다르므로 직접 호출하는 건 위험함
</div>
</details>


