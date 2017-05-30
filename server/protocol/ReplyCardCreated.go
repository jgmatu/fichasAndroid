package protocol

import (
      "io"
)

type ReplyCardCreated struct {
      header *Message
      response string
}

func NewReplyCardCreated() *ReplyCardCreated {
      return &ReplyCardCreated{ProdMsg(RESPCREATECARD), "Card created"}
}

func (reply ReplyCardCreated) Write(tx io.Writer) {
      reply.header.Write(tx)
      WriteString(tx, reply.response)
}

func (reply ReplyCardCreated) Read(rx io.Reader) {
      reply.response = ReadString(rx)
}
