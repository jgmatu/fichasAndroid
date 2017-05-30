package main

import (
      "fmt"
      "os"
      "bufio"
      "io"
      "net"
      "log"
      "strings"
      "strconv"
      "errors"
      "protocol"
      "sync"
)

const (
      DECIMALS = 5
      LENCARDTXT = 5
      LENROUTETXT = 6
      FCARDS = 1
      FROUTES = 2
)

var mutex = &sync.Mutex{}

func readLines(f io.Reader) []string {
      scanner := bufio.NewScanner(f);
      lines := make([]string , 0);

      for scanner.Scan() {
            lines = append(lines, scanner.Text());
      }
      if err := scanner.Err(); err != nil {
            fmt.Fprintln(os.Stderr, "reading standard input:", err);
      }
      return lines;
}

func openFile(path string) []string  {
      f, err := os.OpenFile(path, os.O_RDONLY|os.O_CREATE, 0600);

      if err != nil {
            log.Fatalf("Error open file... %v",  err)
      }
      data := make([]string, 0)
      data = readLines(f)
      f.Close()
      return data
}

func showLines(lines []string)  {
      for i,l := range lines {
            fmt.Printf("Line %d : %s\n", i, l)
      }
}

func getInt32(s string) int32 {
      i, err := strconv.ParseInt(s, 10 , 32)

      if err != nil {
            log.Fatal("Error... reading field I hope int...\n")
      }
      return int32(i)
}

func getFloat(s string) float64 {
      f, err := strconv.ParseFloat(s, 64)

      if err != nil {
            log.Fatal("Error... reading field I hope real...\n")
      }
      return f
}

func getHCard(fcard []string) *protocol.MetaCard {
      if len(fcard) != LENCARDTXT {
            log.Fatal("Error... reading card data, bad format filee in line...\n")
      }
      i := getInt32(fcard[0])
      n := fcard[1]
      d := fcard[2]
      s := fcard[3]
      c := fcard[4]

      return protocol.NewMeta(i, n, d, s, c)
}

func getLocation(fields []string) *protocol.Location {
      ltd := getFloat(fields[0])
      lng := getFloat(fields[1])
      rat := getInt32(fields[2])

      return protocol.NewLocationFillOut(ltd, lng, rat)
}

func getRoute(froute []string) *protocol.Entry {
      if len(froute) != LENROUTETXT {
            log.Fatal("Error... reading route data, bad format file in line...\n")
      }

      l := getLocation(froute[2:5])
      img := froute[5]
      return protocol.NewEntryFillOut(img, l)
}

func isRoute(h *protocol.MetaCard, route []string) bool {
      return h.IsEquals(getInt32(route[0]), route[1])
}

func getRoutes(h *protocol.MetaCard, froutes []string) []protocol.Entry  {
      routes := make([]protocol.Entry, 0)

      for _, r := range froutes {
            formatRoute := strings.Split(r, ",")
            if len(formatRoute) == 0 {
                  continue
            }
            if (isRoute(h, formatRoute)) {
                  route := getRoute(formatRoute)
                  routes = append(routes, *route)
            }
      }
      return routes
}

func getCards(fcards, froutes []string) *protocol.Cards {
      cards := protocol.NewCards()

      for _, c := range fcards {
            formatCard := strings.Split(c, ",")
            if len(formatCard) == 0 {
                  continue
            }
            metaCard := getHCard(formatCard)
            card := protocol.NewCardFillOut(metaCard, getRoutes(metaCard, froutes))
            cards.Add(*card)
      }
      return cards
}

func readData(patmetaCards, pathRoutes string) *protocol.Cards {
      fcards := os.Args[FCARDS]
      froutes := os.Args[FROUTES]

      dataCards := openFile(fcards)
      dataRoutes := openFile(froutes)

      return getCards(dataCards, dataRoutes)
}

func appendCard(card protocol.Card)  {
      f, err := os.OpenFile(os.Args[FCARDS], os.O_APPEND|os.O_WRONLY, 0600)

      if err != nil {
            log.Fatal(err)
      }
      defer f.Close()
      fmt.Fprintf(f, "%s", card.FileFormatCard())
}

func appendEntries(card protocol.Card) {
      f, err := os.OpenFile(os.Args[FROUTES], os.O_APPEND|os.O_WRONLY, 0600)

      if err != nil {
            log.Fatal(err)
      }
      defer f.Close()
      fmt.Fprintf(f, "%s", card.FileFormatEntries())
}

/**
 * Create the metada on the server of one card, that is, create the
 * card on files and storage the card in memmory while running.
 */
func createCard(cards *protocol.Cards, card protocol.Card) {
      appendCard(card)
      appendEntries(card)
      cards.Add(card)
}

/**
 * we response a reply error when the card is already created
 * on the server or when there is not cards near of client whose
 * request cards from its actual location.
 */
func replyError(conn net.Conn, err error) {
      reply := protocol.NewReplyError(err)
      reply.Write(conn)
}

/**
 * Request Cards, receive a message from client request a cards
 * when the request is readed the server calculate the ratio
 * cards from location send in the payload of message and
 * reply the cards near of 10 km of ratio...
 *
 */
func requestCards(conn net.Conn, cards *protocol.Cards) {
      request := protocol.NewRequestCards()

      request.Read(conn)
      mutex.Lock()
      if err, c := request.GetCards(*cards); err == nil {
            protocol.NewReplyCards(c).Write(conn)
      } else {
            replyError(conn, errors.New("Not cards founds..."))
      }
      mutex.Unlock()
}

/**
 * This funcion process a request from client to create
 * a card, when the request is readed the card is checked
 * on the repository server, if the card not exist is created
 * and reply card created is sended.
 */
func requestCreateCard(conn net.Conn, cards *protocol.Cards) {
      request := protocol.NewRequestCreateCard()

      request.Read(conn)
      mutex.Lock()
      if err, card := request.CreateCard(*cards); err == nil {
            protocol.NewReplyCardCreated().Write(conn)
            createCard(cards, card)
      } else {
            replyError(conn, errors.New("Card already created..."))
      }
      mutex.Unlock()
}

/**
 * Handle the connection on the server the possible messages received
 * are request a card and create a card, the server wait to receive
 * any of this messages.
 *
 */
func handleConn(conn net.Conn, cards *protocol.Cards) {
      defer conn.Close()
      msg := protocol.NewMsg()

      msg.Read(conn)
      if (msg.IsReqCards()) {
            requestCards(conn, cards)
      } else if (msg.IsReqCreateCard()) {
            requestCreateCard(conn, cards)
      } else {
            replyError(conn, errors.New("Bad protocol message..."))
      }
}

func connect(cards *protocol.Cards) {
      listener, err := net.Listen("tcp", "localhost:2000")
      if err != nil {
            log.Fatal(err)
      }
      for {
            conn, err := listener.Accept()
            if err != nil {
                  log.Print(err) // e.g., connection refused.
                  continue
            }
            go handleConn(conn, cards)
      }
}

func main() {
      if len(os.Args[1:]) != 2 {
            return
      }
      cards := readData(os.Args[1], os.Args[2])
      cards.CreateDirs()
      connect(cards)
}
