package protocol


import (
      "io"
)

type ReplyCards struct {
      header *Message
      cards *Cards
}


func NewReplyCards(cards *Cards) *ReplyCards {
      return &ReplyCards{ProdMsg(RESPCARDS), cards}
}

func (reply ReplyCards) Write(tx io.Writer) {
      reply.header.Write(tx)
      reply.cards.Write(tx)
}
