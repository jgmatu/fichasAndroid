package protocol

import (
      "log"
      "io"
      "os"
      "encoding/binary"
      "math"
)

type Message struct {
      _type int32
}

const (
      REQCARDS = 0
      RQCREATECARD = 1
      RESPCARDS = 2
      RESPCREATECARD = 3
      RESPERR = 4
      DECIMALS = 5
      MAXBYTES = 65536
)

func ProdMsg(_type int32) *Message {
      return &Message{_type}
}

func NewMsg() *Message {
      return &Message{-1}
}

func (msg *Message) IsReqCards() bool {
      return msg._type == REQCARDS
}

func (msg *Message) IsReqCreateCard() bool {
      return msg._type == RQCREATECARD
}

func (msg *Message) Write(tx io.Writer) {
      WriteInt32(tx, msg._type)
}

func (msg *Message) Read(rx io.Reader) {
      msg._type = ReadInt32(rx)
}

func ReadInt32(rx io.Reader) int32 {
      var value int32
      err := binary.Read(rx, binary.BigEndian, &value)

      if err != nil {
          log.Fatalf("Error reading Integer : %v", err)
      }
      return value
}

func WriteInt32(tx io.Writer, value int32) {
      err := binary.Write(tx, binary.BigEndian, value)

      if err != nil {
          log.Fatalf("Error writing Integer : %v", err)
      }
}

func WriteFloat(tx io.Writer, numb float64) {
      n := int32(numb)
      d := int32((numb - float64(n)) * math.Pow10(DECIMALS))

      WriteInt32(tx, n)
      WriteInt32(tx, d)
      WriteInt32(tx, DECIMALS)
}

func ReadFloat(rx io.Reader) float64 {
      numb := float64(ReadInt32(rx))
      dec := float64(ReadInt32(rx))
      numDec := int(ReadInt32(rx))

      return numb + dec / math.Pow10(numDec)
}

func ReadString(rx io.Reader) string {
      length := int(ReadInt32(rx))
      data := make([]byte, length)

	if _, err := io.ReadFull(rx, data); err != nil {
		log.Fatalf("Error reading string : %v", err)
	}
      return string(data)
}

func WriteString(tx io.Writer, data string) {
      length := int32(len(data));

      WriteInt32(tx, length)
      if _, err := io.WriteString(tx, data); err != nil {
            log.Fatal("Error writing string : %v", err)
      }
}

func WriteImage(tx io.Writer, path string) {
      f, err := os.OpenFile(path, os.O_RDONLY, 0600)

      if err != nil {
            log.Fatalf("Error : %v\n", err)
      }
      defer f.Close()

      fi, err := f.Stat()
      if err != nil {
            log.Fatalf("Error getting file stat : %v\n", err)
      }
      size := int(fi.Size())
      WriteInt32(tx, int32(size))
      for size > 0 {
            nr := MAXBYTES
            if size < MAXBYTES {
                  nr = size
            }
            data := make([]byte, nr)
            nr, err := f.Read(data)

            if err == io.EOF && nr == 0 {
                  break;
            } else if err != nil {
                  log.Fatalf("Error reading image from file : %v\n", err)
            }
            if _, err := tx.Write(data[0:nr]) ; err != nil {
                  log.Fatalf("Error sending image from socket... : %v\n", err)
            }
            size -= nr;
      }
}

func ReadImage(rx io.Reader, path string) {
      f, err := os.Create(path)

      if err != nil {
            log.Fatalf("Error : creating image file... %v\n", err)
      }
      defer f.Close()

      size := int(ReadInt32(rx))
      for size > 0 {
            nr := MAXBYTES
            if size < MAXBYTES {
                  nr = size;
            }
            data := make([]byte, nr)
            nr, err := rx.Read(data)

            if nr == 0 && err == io.EOF {
                  break
            } else if err != nil {
                  log.Fatalf("Error reading from network : %v\n", err)
            }
            if _, err := f.Write(data[0:nr]); err != nil {
                  log.Fatalf("Error writing in disk the image : %v", err)
            }
            size -= nr;
      }
}
