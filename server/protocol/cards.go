package protocol

import (
      "io"
      "fmt"
      "errors"
)

type Cards struct {
      cards []Card
}

func NewCardsFillOut(c []Card) *Cards {
      return &Cards{c}
}

func NewCards() *Cards {
      return &Cards{make([]Card, 0)}
}

func (cards *Cards) Add(c Card) {
      cards.cards = append(cards.cards, c)
}

func (cards Cards) Contains(card Card) bool {
      for _,c := range cards.cards {
            if card.head.IsEquals(c.head.id, c.head.name) {
                  return true
            }
      }
      return false
}

func (c Cards) Write(tx io.Writer) {
      WriteInt32(tx, int32(len(c.cards)))

      for _, c := range c.cards {
            c.Write(tx)
      }
}

func (c *Cards) Read(rx io.Reader) {
      length := ReadInt32(rx)

      c.cards = make([]Card, length)
      for i, card := range c.cards {
            card.Read(rx)
            c.cards[i] = card
      }
}

func (c Cards) GetCardsFromLocation(loc Location) (error, *Cards) {
      cards := NewCards()

      for _,card := range c.cards {
            if (!card.IsMaxDistance(loc)) {
                  cards.Add(card)
            }
      }
      if len(cards.cards) == 0 {
            return errors.New("Cards not found..."), nil
      }
      return nil, cards
}

func (c Cards) CreateDirs() error {
      for _, card := range c.cards {
            dir := fmt.Sprintf("images/%d%s", card.head.id, card.head.name);
            if err := card.CreateDir(dir); err != nil {
                  return err
            }
      }
      return nil
}

func (c Cards) String() string {
      result := ""

      for _,card := range c.cards {
            result += fmt.Sprintf("\nCard : %s\n", card)
      }
      return result
}
