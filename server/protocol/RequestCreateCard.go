package protocol

import (
      "io"
      "errors"
)

type RequestCreateCard struct {
      header *Message
      card Card
}

func NewRequestCreateCard() *RequestCreateCard {
      return &RequestCreateCard{ProdMsg(RQCREATECARD), Card{}}
}

func (request RequestCreateCard) Write(tx io.Writer) {
      request.header.Write(tx)
      request.card.Write(tx)
}

func (request *RequestCreateCard) Read(rx io.Reader) {
      request.card.Read(rx)
}

func (request RequestCreateCard) CreateCard(cards Cards) (error, Card) {
      if (cards.Contains(request.card)) {
            return errors.New("The card is already created..."), Card{}
      }
      return nil, request.card
}
