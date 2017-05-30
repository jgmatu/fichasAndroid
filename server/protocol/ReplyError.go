package protocol

import (
      "io"
)

type ReplyError struct {
      header *Message
      err error
}

func NewReplyError(err error) *ReplyError {
      return &ReplyError{ProdMsg(RESPERR), err}
}

func (reply ReplyError) Write(tx io.Writer) {
      reply.header.Write(tx)
      WriteString(tx, reply.err.Error())
}
