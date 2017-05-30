package protocol

import (
      "io"
      "errors"
)

type RequestCards struct {
      header *Message
      loc *Location
}

func NewRequestCards() *RequestCards {
      return &RequestCards{ProdMsg(REQCARDS), NewLocation()}
}

func (request RequestCards) Write(tx io.Writer) {
      request.header.Write(tx)
      request.loc.Write(tx)
}

func (request RequestCards) Read(rx io.Reader)  {
      request.loc.Read(rx)
}

func (request RequestCards) GetCards(cards Cards) (error, *Cards) {
      c := NewCards()

      for _, card := range cards.cards {
            if !card.IsMaxDistance(*request.loc) {
                  c.Add(card)
            }
      }
      if len(c.cards) == 0 {
            return errors.New("No cards founds..."), c
      }
      return nil, c
}
