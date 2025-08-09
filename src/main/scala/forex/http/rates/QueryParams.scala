package forex.http.rates

import cats.syntax.either._
import forex.domain.{Currency, Rate}
import org.http4s.dsl.impl.{OptionalMultiQueryParamDecoderMatcher, QueryParamDecoderMatcher}
import org.http4s.{ParseFailure, QueryParamDecoder}

object QueryParams {

  private[http] implicit val currencyQueryParam: QueryParamDecoder[Currency] =
    QueryParamDecoder[String].map(Currency.fromString)

  object FromQueryParam extends QueryParamDecoderMatcher[Currency]("from")
  object ToQueryParam extends QueryParamDecoderMatcher[Currency]("to")

  implicit val pairQueryParamsDecoder: QueryParamDecoder[Rate.Pair] =
    QueryParamDecoder[String].emap { str =>
      if (str.length != 6)
        Left(ParseFailure("Invalid format", s"Invalid pair format: $str"))
      else {
        val fromStr = str.take(3)
        val toStr = str.drop(3)
        for {
          from <- Either
            .catchNonFatal(Currency.fromString(fromStr))
            .leftMap(_ => ParseFailure("Invalid currency", s"Invalid currency: $fromStr"))
          to <- Either
            .catchNonFatal(Currency.fromString(toStr))
            .leftMap(_ => ParseFailure("Invalid currency", s"Invalid currency: $toStr"))
        } yield Rate.Pair(from, to)
      }
    }

  object PairQueryParam extends OptionalMultiQueryParamDecoderMatcher[Rate.Pair]("pair")
}
